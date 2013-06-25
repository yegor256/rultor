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
import com.jcabi.log.VerboseThreads;
import com.rultor.spi.Conveyer;
import com.rultor.spi.Conveyer.Log;
import com.rultor.spi.Instance;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

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
public final class SimpleConveyer
    implements Conveyer, Closeable, Callable<Void> {

    /**
     * In how many threads we run instances.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * Tv.TEN;

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
     * Log appender.
     */
    private final transient ConveyerAppender appender;

    /**
     * Counter of executed jobs.
     */
    private transient Counter counter;

    /**
     * Consumer of new specs from Queue.
     */
    private final transient ExecutorService consumer =
        Executors.newSingleThreadExecutor(
            new VerboseThreads(SimpleConveyer.class)
        );

    /**
     * Executor of instances.
     */
    private final transient ExecutorService executor =
        Executors.newCachedThreadPool(
            new ThreadFactory() {
                private final transient AtomicLong group = new AtomicLong();
                @Override
                public Thread newThread(final Runnable runnable) {
                    return new Thread(
                        new ThreadGroup(
                            Long.toString(this.group.incrementAndGet())
                        ),
                        runnable
                    );
                }
            }
        );

    /**
     * Public ctor.
     * @param que The queue of specs
     * @param rep Repo
     * @param usrs Users
     * @param log Log
     * @checkstyle ParameterNumber (4 lines)
     */
    public SimpleConveyer(@NotNull final Queue que, @NotNull final Repo rep,
        @NotNull final Users usrs, @NotNull final Log log) {
        this.queue = que;
        this.repo = rep;
        this.users = usrs;
        this.appender = new ConveyerAppender(log);
        this.appender.setThreshold(Level.DEBUG);
        this.appender.setLayout(new PatternLayout("%m"));
        Logger.getRootLogger().addAppender(this.appender);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        this.consumer.submit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        Logger.getRootLogger().removeAppender(this.appender);
        this.appender.close();
        this.consumer.shutdown();
        this.executor.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
    public Void call() throws Exception {
        while (true) {
            this.submit(this.queue.pull());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(@NotNull final MetricRegistry registry) {
        this.counter = registry.counter(
            MetricRegistry.name(this.getClass(), "done-jobs")
        );
    }

    /**
     * Submit work for execution in the threaded executor.
     * @param work Work
     */
    private void submit(final Work work) {
        this.executor.submit(
            new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final Instance unit = new LoggableInstance(
                        SimpleConveyer.this.repo.make(
                            SimpleConveyer.this.users.fetch(work.owner()),
                            work.spec()
                        ),
                        SimpleConveyer.this.appender,
                        work
                    );
                    unit.pulse();
                    SimpleConveyer.this.counter.inc();
                    return null;
                }
            }
        );
    }

}
