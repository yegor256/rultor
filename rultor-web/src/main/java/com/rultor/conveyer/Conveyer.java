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
import com.jcabi.log.VerboseThreads;
import com.rultor.queue.Queue;
import com.rultor.repo.Instance;
import com.rultor.repo.Repo;
import com.rultor.repo.State;
import com.rultor.users.Spec;
import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Execution conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.INFO)
@ToString
@EqualsAndHashCode(of = { "queue", "repo" })
@SuppressWarnings("PMD.DoNotUseThreads")
public final class Conveyer implements Closeable, Callable<Void> {

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
     * Repository.
     */
    private final transient Repo repo;

    /**
     * State to use for everybody.
     */
    private final transient State state = new State.Memory();

    /**
     * Consumer of new specs from Queue.
     */
    private final transient ExecutorService consumer =
        Executors.newSingleThreadExecutor(new VerboseThreads(Conveyer.class));

    /**
     * Executor of instances.
     */
    private final transient ExecutorService executor =
        Executors.newCachedThreadPool(new VerboseThreads(Conveyer.class));

    /**
     * Public ctor.
     * @param que The queue of specs
     * @param rep Repo
     */
    public Conveyer(final Queue que, final Repo rep) {
        this.queue = que;
        this.repo = rep;
    }

    /**
     * Start it.
     */
    public void start() {
        this.consumer.submit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.consumer.shutdown();
        this.executor.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void call() throws Exception {
        while (true) {
            final Spec spec = this.queue.pull();
            final Instance instance = this.repo.make(spec);
            this.submit(instance);
        }
    }

    /**
     * Submit an instance.
     * @param instance The instance
     */
    private void submit(final Instance instance) {
        this.executor.submit(
            new Runnable() {
                @Override
                public void run() {
                    instance.pulse(Conveyer.this.state);
                }
            }
        );
    }

}
