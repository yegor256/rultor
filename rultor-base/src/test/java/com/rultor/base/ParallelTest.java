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
import com.rultor.spi.Instance;
import com.rultor.stateful.Lineup;
import com.rultor.stateful.Notepad;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
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
     * @checkstyle ExecutableStatementCount (200 lines)
     */
    @Test
    public void enablesFixedNumberOfParallelThreads() throws Exception {
        final Instance origin = Mockito.mock(Instance.class);
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock inv)
                    throws Exception {
                    TimeUnit.SECONDS.sleep(1);
                    return null;
                }
            }
        ).when(origin).pulse();
        final Collection<String> active = new CopyOnWriteArrayList<String>();
        final Notepad notepad = Mockito.mock(Notepad.class);
        Mockito.doAnswer(
            new Answer<Integer>() {
                @Override
                public Integer answer(final InvocationOnMock inv) {
                    return active.size();
                }
            }
        ).when(notepad).size();
        Mockito.doAnswer(
            new Answer<Boolean>() {
                @Override
                public Boolean answer(final InvocationOnMock inv) {
                    return active.add(inv.getArguments()[0].toString());
                }
            }
        ).when(notepad).add(Mockito.anyString());
        Mockito.doAnswer(
            new Answer<Boolean>() {
                @Override
                public Boolean answer(final InvocationOnMock inv) {
                    return active.remove(inv.getArguments()[0].toString());
                }
            }
        ).when(notepad).remove(Mockito.anyString());
        final int threads = 10;
        final int maximum = 3;
        final Parallel parallel = new Parallel(
            maximum,
            new Lineup.Asynchronous(),
            notepad,
            origin
        );
        final ExecutorService svc = Executors.newFixedThreadPool(
            threads, new VerboseThreads()
        );
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                parallel.pulse();
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
        Mockito.verify(origin, Mockito.atMost(maximum)).pulse();
    }

}
