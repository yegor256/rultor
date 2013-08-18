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
package com.rultor.drain;

import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.spi.Drain;
import com.rultor.spi.Work;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link BufferedWrite}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("unchecked")
public final class BufferedWriteTest {

    /**
     * BufferedWrite can be converted to string.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsItselfInString() throws Exception {
        MatcherAssert.assertThat(
            new BufferedWrite(new Work.None(), 2, new Trash()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * BufferedWrite can send data through.
     * @throws Exception If some problem inside
     */
    @Test
    public void sendsLinesThrough() throws Exception {
        final Drain drain = Mockito.mock(Drain.class);
        final Drain first = new BufferedWrite(
            new Work.Simple(new URN("urn:facebook:8"), "test-99"),
            2, drain
        );
        final Drain second = new BufferedWrite(
            new Work.Simple(new URN("urn:facebook:9"), "test-88"),
            Tv.FIVE, Mockito.mock(Drain.class)
        );
        final String line = "some \t\u20ac\tfdsfs9980 Hello878";
        final CountDownLatch done = new CountDownLatch(1);
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock inv)
                    throws Exception {
                    first.append(Arrays.asList(line));
                    second.append(Arrays.asList(line));
                    done.countDown();
                    return null;
                }
            }
        ).when(drain).append(Mockito.any(Iterable.class));
        first.append(Arrays.asList(line));
        second.append(Arrays.asList(line));
        assert done.await(Tv.FIVE, TimeUnit.SECONDS);
        Mockito.verify(drain).append(
            Mockito.<Iterable<String>>argThat(
                Matchers.<String>everyItem(Matchers.equalTo(line))
            )
        );
    }

    /**
     * BufferedWrite can write and flush at the same time.
     * @throws Exception If some problem inside
     */
    @Test
    public void writesAndFlushes() throws Exception {
        final AtomicLong count = new AtomicLong();
        final Drain origin = Mockito.mock(Drain.class);
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock inv) {
                    for (Object line
                        : Iterable.class.cast(inv.getArguments()[0])) {
                        final long number = count.getAndIncrement();
                        if (number != Long.parseLong(line.toString())) {
                            count.set(0);
                        }
                    }
                    return null;
                }
            }
        ).when(origin).append(Mockito.any(Iterable.class));
        final Drain drain = new BufferedWrite(
            new Work.Simple(new URN("urn:test:9"), "f"), 2, origin
        );
        final long total = TimeUnit.SECONDS.toMillis(Tv.FIVE);
        for (int idx = 0; idx < total; ++idx) {
            drain.append(Arrays.asList(String.format("%d", idx)));
            TimeUnit.MILLISECONDS.sleep(1);
        }
        MatcherAssert.assertThat(count.get(), Matchers.equalTo(total));
    }

}
