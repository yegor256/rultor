/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.stateful.sdb;

import com.jcabi.simpledb.Item;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link ItemLineup}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TestClassWithoutTestCases" })
public final class ItemLineupTest {
    /**
     * ItemLineUp should execute given callable when Item is empty.
     * @throws Exception In case of error.
     */
    @Test
    public void executesCallableWhenItemEmpty() throws Exception {
        final String finish = "finish";
        final Item item = new ItemLineupTest.TestItem();
        MatcherAssert.assertThat(
            new ItemLineup(item).exec(
                new Callable<String>() {
                    @Override
                    public String call() {
                        return finish;
                    }
                }
            ),
            Matchers.equalTo(finish)
        );
    }

    /**
     * ItemLineUp should execute two runnables synchronizing on the same Item.
     * @throws Exception In case of error.
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void executesTwoRunnables() throws Exception {
        final int tcount = 2;
        final AtomicInteger count = new AtomicInteger(0);
        final Item item = new ItemLineupTest.TestItem();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(2);
        for (int threads = 0; threads < tcount; ++threads) {
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            start.await();
                        } catch (final InterruptedException ex) {
                            throw new IllegalStateException(ex);
                        }
                        new ItemLineup(item).exec(
                            new Runnable() {
                                @Override
                                public void run() {
                                    count.incrementAndGet();
                                }
                            }
                        );
                        finish.countDown();
                    }
                }
            ).start();
        }
        start.countDown();
        finish.await();
        MatcherAssert.assertThat(
            count.get(),
            Matchers.equalTo(tcount)
        );
    }

    /**
     * Test item implementation.
     */
    private static final class TestItem
        extends HashMap<String, String> implements Item {

        /**
         * Serial version.
         */
        private static final long serialVersionUID = -5144719771518328320L;

        @Override
        public String name() {
            return "name";
        }
    }
}
