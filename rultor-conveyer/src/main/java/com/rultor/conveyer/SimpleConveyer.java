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

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.rultor.conveyer.http.HttpServer;
import com.rultor.conveyer.http.Streams;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Instance;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Users;
import java.io.Closeable;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Horizontally scalable execution conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @todo #158 Remove total field from this class and refactor callers to call
 *  constructor without this parameter.
 */
@Loggable(Loggable.INFO)
@ToString
@EqualsAndHashCode(of = "queue")
@SuppressWarnings("PMD.DoNotUseThreads")
final class SimpleConveyer implements Closeable {

    /**
     * HTTP port we're listening to.
     */
    private static final int PORT = 9095;

    /**
     * How many threads to run in parallel.
     */
    private final transient int total;

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
    private final transient Streams streams = new Log4jStreams();

    /**
     * HTTP server.
     */
    private final transient HttpServer server;

    /**
     * Threads.
     */
    private final transient ConveyerThreads threads;

    /**
     * Threads that read from queue and submit tasks to processors.
     */
    private final transient ExecutorService providers;

    /**
     * Threads that execute tasks.
     */
    private final transient ExecutorService processors;

    /**
     * Public ctor.
     * @param que The queue of specs
     * @param rep Repo
     * @param usrs Users
     * @param max Total number of threads
     * @throws IOException If fails
     * @checkstyle ParameterNumber (4 lines)
     */
    protected SimpleConveyer(
        @NotNull(message = "queue can't be NULL") final Queue que,
        @NotNull(message = "repo can't be NULL") final Repo rep,
        @NotNull(message = "users can't be NULL") final Users usrs,
        final int max)
        throws IOException {
        this.queue = que;
        this.repo = rep;
        this.users = usrs;
        this.total = max;
        this.server = new HttpServer(this.streams, SimpleConveyer.PORT);
        this.threads = new ConveyerThreads();
        this.processors = Executors.newCachedThreadPool();
        this.providers = Executors.newSingleThreadExecutor();
    }

    /**
     * Start the conveyer.
     */
    public void start() {
        this.providers.submit(
            new Callable<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    SimpleConveyer.this.process();
                    SimpleConveyer.this.providers.submit(this);
                    return null;
                }
            }
        );
        this.server.listen();
    }

    @Override
    @Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
    public void close() throws IOException {
        try {
            this.stop(this.providers, TimeUnit.SECONDS.toMillis(1));
            this.stop(this.processors, TimeUnit.HOURS.toMillis(2));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        this.server.close();
        this.streams.close();
    }

    /**
     * Stop given ExecutorService.
     * @param exec ExecutorService to stop.
     * @param millis How long to wait before forceful shutdown.
     * @throws InterruptedException When thread interrupted.
     */
    private void stop(final ExecutorService exec, final long millis)
        throws InterruptedException {
        final Random rand = new Random();
        final long start = System.currentTimeMillis();
        while (true) {
            final long age = System.currentTimeMillis() - start;
            if (age > millis) {
                exec.shutdownNow();
            } else {
                exec.shutdown();
            }
            if (exec.awaitTermination(1, TimeUnit.SECONDS)) {
                break;
            }
            TimeUnit.SECONDS.sleep(rand.nextInt(Tv.HUNDRED));
            Logger.info(
                this, "waiting %[ms]s for threads termination", age
            );
        }
    }

    /**
     * Process the next work from the queue.
     * @throws InterruptedException If interrupted
     */
    private void process() throws InterruptedException {
        final Coordinates work = this.queue.pull(1, TimeUnit.SECONDS);
        if (!work.equals(new Coordinates.None())) {
            SimpleConveyer.this.processors.submit(
                new VerboseRunnable(
                    new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            SimpleConveyer.this.process(work);
                            return null;
                        }
                    },
                    true, false
                )
            );
        }
    }

    /**
     * Process this given work.
     * @param work Work to process
     */
    private void process(final Coordinates work) {
        this.threads.label(work.toString());
        final String key = this.streams.register();
        try {
            new Job(work, this.repo, this.users).process(
                new Job.Decor() {
                    @Override
                    public Instance decorate(final Instance instance) {
                        return new WithCoords(
                            work,
                            new WithStdout(SimpleConveyer.PORT, key, instance)
                        );
                    }
                }
            );
        } finally {
            this.streams.unregister(key);
            this.threads.label("free");
        }
    }

}
