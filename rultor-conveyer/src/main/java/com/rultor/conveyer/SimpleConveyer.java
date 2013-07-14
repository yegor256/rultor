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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.urn.URN;
import com.rultor.spi.Expense;
import com.rultor.spi.Instance;
import com.rultor.spi.Invoice;
import com.rultor.spi.Metricable;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.Time;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Work;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
 */
@Loggable(Loggable.INFO)
@ToString
@EqualsAndHashCode(of = "queue")
@SuppressWarnings("PMD.DoNotUseThreads")
public final class SimpleConveyer implements Closeable, Metricable {

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
     * Counter of currently running jobs.
     */
    private transient Counter counter;

    /**
     * Consumer and executer of new specs from Queue.
     */
    private final transient ScheduledExecutorService svc =
        Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors() * Tv.TEN,
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
     * @checkstyle ParameterNumber (4 lines)
     */
    public SimpleConveyer(
        @NotNull(message = "queue can't be NULL") final Queue que,
        @NotNull(message = "repo can't be NULL") final Repo rep,
        @NotNull(message = "users can't be NULL") final Users usrs) {
        this.queue = que;
        this.repo = rep;
        this.users = usrs;
    }

    /**
     * Start the conveyer.
     */
    public void start() {
        this.svc.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        SimpleConveyer.this.process();
                        return null;
                    }
                },
                true, false
            ),
            TimeUnit.SECONDS.toMicros(1), 1, TimeUnit.MICROSECONDS
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
    public void close() throws IOException {
        final Random rand = new Random();
        try {
            while (true) {
                this.svc.shutdown();
                if (this.svc.awaitTermination(1, TimeUnit.SECONDS)) {
                    break;
                }
                TimeUnit.SECONDS.sleep(rand.nextInt(Tv.HUNDRED));
                com.jcabi.log.Logger.info(
                    this, "waiting for threads termination"
                );
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final MetricRegistry registry) {
        this.counter = registry.counter(
            MetricRegistry.name(this.getClass(), "done-jobs")
        );
    }

    /**
     * Process the next work from the queue.
     * @throws Exception If fails
     */
    private void process() throws Exception {
        final Work work = this.queue.pull(1, TimeUnit.SECONDS);
        if (!work.equals(new Work.None())) {
            if (this.counter != null) {
                this.counter.inc();
            }
            try {
                this.process(work);
            } finally {
                if (this.counter != null) {
                    this.counter.dec();
                }
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
        final Variable<?> var =
            new Repo.Cached(this.repo, owner, work.spec()).get();
        final Collection<Expense> expenses =
            new CopyOnWriteArrayList<Expense>();
        final Object object = var.instantiate(
            this.users,
            new Work() {
                @Override
                public Time started() {
                    return work.started();
                }
                @Override
                public URN owner() {
                    return work.owner();
                }
                @Override
                public String unit() {
                    return work.unit();
                }
                @Override
                public Spec spec() {
                    return work.spec();
                }
                @Override
                public void spent(final Expense exp) {
                    expenses.add(exp);
                }
            }
        );
        if (object instanceof Instance) {
            try {
                Instance.class.cast(object).pulse();
            } finally {
                this.users.get(work.owner()).statement().add(
                    new Invoice.Composed(expenses)
                );
            }
        }
    }

}
