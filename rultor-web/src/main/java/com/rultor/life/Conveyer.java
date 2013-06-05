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
package com.rultor.life;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseThreads;
import com.rultor.om.Instance;
import com.rultor.om.Repo;
import com.rultor.om.Spec;
import com.rultor.om.State;
import com.rultor.queue.Queue;
import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.EqualsAndHashCode;

/**
 * Execution conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.INFO)
@EqualsAndHashCode(of = { "queue", "repo" })
final class Conveyer implements Closeable {

    /**
     * In how many threads consume.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * Tv.FOUR;

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
    private final transient State state = new MemState();

    /**
     * Executor.
     */
    private final transient ExecutorService svc = Executors.newFixedThreadPool(
        Conveyer.THREADS,
        new VerboseThreads(Conveyer.class)
    );

    /**
     * Public ctor.
     * @param que The queue of specs
     * @param rep Repo
     */
    protected Conveyer(final Queue que, final Repo rep) {
        this.queue = que;
        this.repo = rep;
    }

    /**
     * Start it.
     */
    public void start() {
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Spec spec = Conveyer.this.queue.pull();
                final Instance instance = Conveyer.this.repo.make(spec);
                instance.pulse(state);
                return null;
            }
        };
        for (int thread = 0; thread < Conveyer.THREADS; ++thread) {
            this.svc.submit(callable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.svc.shutdown();
    }

}
