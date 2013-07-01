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

import com.jcabi.urn.URN;
import com.rultor.spi.Drain;
import com.rultor.spi.Spec;
import com.rultor.spi.Time;
import com.rultor.spi.Work;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link BufferedWrite}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class BufferedWriteTest {

    /**
     * BufferedWrite can save and show.
     * @throws Exception If some problem inside
     */
    @Test
    public void savesDataAndRenders() throws Exception {
        final Work work = new Work.Simple(
            new URN("urn:facebook:11"), "test", new Spec.Simple()
        );
        final Drain drain = new BufferedWrite(work, 1, new Trash());
        final String line = "some \t\u20ac\tfdsfs Hello878";
        drain.append(Arrays.asList(line));
        MatcherAssert.assertThat(
            drain.pulses(),
            Matchers.<Time>iterableWithSize(0)
        );
        MatcherAssert.assertThat(
            IOUtils.toString(drain.read(), CharEncoding.UTF_8),
            Matchers.containsString(line)
        );
    }

    /**
     * BufferedWrite can manage parallel requests.
     * @throws Exception If some problem inside
     */
    @Test
    public void handlesParallelWorks() throws Exception {
        final Work first = new Work.Simple(
            new URN("urn:facebook:8888"), "test-1", new Spec.Simple()
        );
        final Work second = new Work.Simple(
            new URN("urn:facebook:999"), "test-2", new Spec.Simple()
        );
        new BufferedWrite(first, 1, new Trash())
            .append(Arrays.asList("first", "second"));
        new BufferedWrite(second, 1, new Trash())
            .append(Arrays.asList("one more line to ANOTHER work"));
        MatcherAssert.assertThat(
            IOUtils.toString(
                new BufferedWrite(first, 1, new Trash()).read()
            ),
            Matchers.endsWith("first\nsecond\n")
        );
    }

    /**
     * BufferedWrite can be converted to string.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsItselfInString() throws Exception {
        MatcherAssert.assertThat(
            new BufferedWrite(new Work.None(), 1, new Trash()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
