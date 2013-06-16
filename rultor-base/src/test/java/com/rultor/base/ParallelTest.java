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
package com.rultor.base;

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseThreads;
import com.jcabi.urn.URN;
import com.rultor.spi.Pulseable;
import com.rultor.spi.Spec;
import com.rultor.spi.State;
import com.rultor.spi.Work;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link Parallel}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class ParallelTest {

    /**
     * Parallel can enable certain amount of parallel threads.
     * @throws Exception If some problem inside
     */
    @Test
    public void enablesFixedNumberOfParallelThreads() throws Exception {
        final Pulseable origin = Mockito.mock(Pulseable.class);
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock inv)
                    throws Exception {
                    TimeUnit.SECONDS.sleep(1);
                    return null;
                }
            }
        ).when(origin).pulse(Mockito.any(Work.class), Mockito.any(State.class));
        final Work work = new Work.Simple(
            new URN("urn:facebook:55"), "unit-name", new Spec.Simple("")
        );
        final State state = new State.Memory();
        final int threads = 10;
        final int maximum = 3;
        final Parallel parallel = new Parallel(maximum, origin);
        final ExecutorService svc = Executors.newFixedThreadPool(
            threads, new VerboseThreads()
        );
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                parallel.pulse(work, state);
                done.countDown();
                return null;
            }
        };
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(callable);
        }
        start.countDown();
        svc.shutdown();
        MatcherAssert.assertThat(
            svc.awaitTermination(Tv.FIVE, TimeUnit.SECONDS),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(done.getCount(), Matchers.equalTo(0L));
        Mockito.verify(origin, Mockito.times(maximum)).pulse(work, state);
    }

}
