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
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link BufferedRead}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class BufferedReadTest {

    /**
     * BufferedRead can save and show.
     * @throws Exception If some problem inside
     */
    @Test
    public void loadsDataAndRenders() throws Exception {
        final Work work = new Work.Simple();
        final Drain drain = Mockito.mock(Drain.class);
        final String text = "\u20ac\n\n\n test \u0433";
        Mockito.doReturn(
            IOUtils.toInputStream(text, CharEncoding.UTF_8)
        ).when(drain).read();
        MatcherAssert.assertThat(
            IOUtils.toString(
                new BufferedRead(work, 2, drain).read(),
                CharEncoding.UTF_8
            ),
            Matchers.containsString(text)
        );
        Mockito.verify(drain).read();
    }

    /**
     * BufferedRead can be converted to string.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsItselfInString() throws Exception {
        MatcherAssert.assertThat(
            new BufferedRead(new Work.None(), 2, new Trash()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * BufferedRead can read and flush at the same time.
     * @throws Exception If some problem inside
     */
    @Test
    public void readsAndFlushes() throws Exception {
        final String content = "hey, \u0443!";
        final Drain origin = Mockito.mock(Drain.class);
        Mockito.doAnswer(
            new Answer<InputStream>() {
                @Override
                public InputStream answer(final InvocationOnMock inv) {
                    try {
                        return IOUtils.toInputStream(
                            content, CharEncoding.UTF_8
                        );
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        ).when(origin).read();
        final Drain drain = new BufferedRead(
            new Work.Simple(new URN("urn:test:9"), "f"), 2, origin
        );
        final long total = TimeUnit.SECONDS.toMillis(Tv.FIVE);
        for (int idx = 0; idx < total; ++idx) {
            MatcherAssert.assertThat(
                IOUtils.toString(drain.read(), CharEncoding.UTF_8),
                Matchers.containsString(content)
            );
            TimeUnit.MILLISECONDS.sleep(1);
        }
    }

}
