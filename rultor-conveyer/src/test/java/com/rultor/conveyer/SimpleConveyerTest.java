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

import com.google.common.collect.ImmutableMap;
import com.jcabi.urn.URN;
import com.rultor.spi.Drain;
import com.rultor.spi.Instance;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link SimpleConveyer}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SimpleConveyerTest {

    /**
     * SimpleConveyer can start and stop.
     * @throws Exception If some problem inside
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    @Test
    public void startsAndStops() throws Exception {
        final Queue queue = Mockito.mock(Queue.class);
        final URN owner = new URN("urn:facebook:1");
        final String name = "unit-name";
        final AtomicBoolean pulled = new AtomicBoolean();
        Mockito.doAnswer(
            new Answer<Work>() {
                @Override
                public Work answer(final InvocationOnMock inv) {
                    if (pulled.get()) {
                        try {
                            TimeUnit.DAYS.sleep(1);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(ex);
                        }
                    }
                    pulled.set(true);
                    return new Work.Simple(owner, name, new Spec.Simple());
                }
            }
        ).when(queue).pull();
        final Repo repo = Mockito.mock(Repo.class);
        final Instance instance = Mockito.mock(Instance.class);
        final CountDownLatch made = new CountDownLatch(1);
        Mockito.doAnswer(
            new Answer<Instance>() {
                @Override
                public Instance answer(final InvocationOnMock invocation)
                    throws Exception {
                    made.countDown();
                    return instance;
                }
            }
        ).doReturn(Mockito.mock(Drain.class))
            .when(repo)
            .make(Mockito.any(User.class), Mockito.any(Spec.class));
        final User user = Mockito.mock(User.class);
        final Unit unit = Mockito.mock(Unit.class);
        Mockito.doReturn(
            new ImmutableMap.Builder<String, Unit>()
                .put(name, unit)
                .build()
        ).when(user).units();
        Mockito.doReturn(new Spec.Simple()).when(unit).drain();
        final Users users = Mockito.mock(Users.class);
        Mockito.doReturn(
            new ImmutableMap.Builder<URN, User>()
                .put(owner, user)
                .build()
        ).when(users).everybody();
        final SimpleConveyer conveyer = new SimpleConveyer(queue, repo, users);
        try {
            conveyer.start();
            MatcherAssert.assertThat(
                made.await(2, TimeUnit.SECONDS), Matchers.is(true)
            );
        } finally {
            TimeUnit.SECONDS.sleep(1);
            conveyer.close();
        }
        Mockito.verify(queue, Mockito.atLeast(1)).pull();
        Mockito.verify(users, Mockito.atLeast(1)).everybody();
        Mockito.verify(instance, Mockito.atLeast(1)).pulse();
    }

}
