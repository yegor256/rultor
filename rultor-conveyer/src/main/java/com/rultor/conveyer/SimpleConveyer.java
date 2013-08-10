/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.conveyer;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.urn.URN;
import com.rexsl.test.RestTester;
import com.rultor.conveyer.http.HttpServer;
import com.rultor.conveyer.http.Streams;
import com.rultor.spi.Arguments;
import com.rultor.spi.Instance;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Horizontally scalable execution conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Loggable(Loggable.INFO)
@ToString
@EqualsAndHashCode(of = "queue")
@SuppressWarnings({
    "PMD.DoNotUseThreads",
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports"
})
final class SimpleConveyer implements Closeable {

    /**
     * HTTP port we're listening to.
     */
    private static final int PORT = 9095;

    /**
     * How many threads to run in parallel.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * Tv.TWENTY;

    /**
     * Where to get public IP from EC2.
     */
    private static final URI META_IP = URI.create(
        "http://169.254.169.254/latest/meta-data/public-ipv4"
    );

    /**
     * Queue.
     */
    private final transient Queue queue;

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Users.
     */
    private final transient Users users;

    /**
     * Streams.
     */
    private final transient Streams streams;

    /**
     * HTTP server.
     */
    private final transient HttpServer server;

    /**
     * Consumer and executer of new specs from Queue.
     */
    private final transient ScheduledExecutorService svc =
        Executors.newScheduledThreadPool(
            SimpleConveyer.THREADS,
            new ThreadFactory() {
                private final transient AtomicLong group = new AtomicLong();
                @Override
                public Thread newThread(final Runnable runnable) {
                    return new Thread(
                        new ThreadGroup(
                            Long.toString(this.group.incrementAndGet())
                        ),
                        runnable,
                        String.format("conveyer-%d", this.group.get())
                    );
                }
            }
        );

    /**
     * Public ctor.
     * @param que The queue of specs
     * @param rep Repo
     * @param usrs Users
     * @throws IOException If fails
     * @checkstyle ParameterNumber (4 lines)
     */
    protected SimpleConveyer(
        @NotNull(message = "queue can't be NULL") final Queue que,
        @NotNull(message = "repo can't be NULL") final Repo rep,
        @NotNull(message = "users can't be NULL") final Users usrs)
        throws IOException {
        this.queue = que;
        this.repo = rep;
        this.users = usrs;
        this.streams = new Log4jStreams();
        this.server = new HttpServer(this.streams, SimpleConveyer.PORT);
        Logger.info(SimpleConveyer.class, "IP: %s", SimpleConveyer.address());
    }

    /**
     * Start the conveyer.
     */
    public void start() {
        final Runnable runnable = new VerboseRunnable(
            new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    SimpleConveyer.this.process();
                    return null;
                }
            },
            true, false
        );
        for (int thread = 0; thread < SimpleConveyer.THREADS; ++thread) {
            this.svc.scheduleWithFixedDelay(
                runnable,
                TimeUnit.SECONDS.toMicros(1), 1, TimeUnit.MICROSECONDS
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
    public void close() throws IOException {
        final Random rand = new Random();
        final long start = System.currentTimeMillis();
        try {
            while (true) {
                final long age = System.currentTimeMillis() - start;
                if (age > TimeUnit.HOURS.toMillis(2)) {
                    this.svc.shutdownNow();
                } else {
                    this.svc.shutdown();
                }
                if (this.svc.awaitTermination(1, TimeUnit.SECONDS)) {
                    break;
                }
                TimeUnit.SECONDS.sleep(rand.nextInt(Tv.HUNDRED));
                Logger.info(
                    this, "waiting %[ms]s for threads termination", age
                );
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        this.server.close();
    }

    /**
     * Process the next work from the queue.
     * @throws Exception If fails
     */
    private void process() throws Exception {
        final Work origin = this.queue.pull(1, TimeUnit.SECONDS);
        if (!origin.equals(new Work.None())) {
            final String key = this.streams.register();
            // @checkstyle AnonInnerLength (50 lines)
            final Work work = new Work() {
                @Override
                public String toString() {
                    return origin.toString();
                }
                @Override
                public Time started() {
                    return origin.started();
                }
                @Override
                public URN owner() {
                    return origin.owner();
                }
                @Override
                public String unit() {
                    return origin.unit();
                }
                @Override
                public Spec spec() {
                    return origin.spec();
                }
                @Override
                public URI stdout() {
                    return UriBuilder.fromUri("http://localhost/")
                        .path("{key}")
                        .host(SimpleConveyer.address())
                        .port(SimpleConveyer.PORT)
                        .build(key);
                }
            };
            try {
                this.process(work);
            } finally {
                this.streams.unregister(key);
            }
        }
    }

    /**
     * Process given work.
     * @param work The work to process
     * @throws Exception If fails
     */
    private void process(final Work work) throws Exception {
        final User owner = this.users.get(work.owner());
        final Unit unit = owner.units().get(work.unit());
        final Variable<?> var =
            new Repo.Cached(this.repo, owner, work.spec()).get();
        if (var.arguments().isEmpty()) {
            final Object object = var.instantiate(
                this.users, new Arguments(work, unit.wallet())
            );
            if (object instanceof Instance) {
                Instance.class.cast(object).pulse();
            }
        }
    }

    /**
     * Fetch my public IP.
     * @return IP
     * @see http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-instance-addressing.html#using-instance-addressing-common
     */
    @Cacheable(forever = true)
    private static String address() {
        String address;
        try {
            address = RestTester.start(SimpleConveyer.META_IP)
                .get("fetch EC2 public IP").getBody();
        } catch (AssertionError ex) {
            address = "localhost";
        }
        return address;
    }

}
