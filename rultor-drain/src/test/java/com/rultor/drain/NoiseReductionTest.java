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
import com.rultor.spi.Work;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link NoiseReduction}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class NoiseReductionTest {

    /**
     * NoiseReduction can filter.
     * @throws Exception If some problem inside
     */
    @Test
    public void filtersNoise() throws Exception {
        final Work first = new Work.Simple(
            new URN("urn:facebook:11"), "test-4", new Spec.Simple()
        );
        final Work second = new Work.Simple(
            new URN("urn:facebook:55"), "test-8", new Spec.Simple()
        );
        final Drain dirty = new BufferedWrite(first, 1, new Trash());
        final Drain clean = new BufferedWrite(second, 1, new Trash());
        final Drain drain = new NoiseReduction(
            "Hello[0-9]+",
            1,
            dirty,
            clean
        );
        final String bad = "somefffffds900-4932%^&$%^&#%@^&!\u20ac\tfdsfs";
        drain.append(Arrays.asList(bad));
        MatcherAssert.assertThat(
            IOUtils.toString(dirty.read(), CharEncoding.UTF_8),
            Matchers.containsString(bad)
        );
        MatcherAssert.assertThat(
            IOUtils.toString(clean.read(), CharEncoding.UTF_8),
            Matchers.not(Matchers.containsString(bad))
        );
    }

    /**
     * NoiseReduction can be converted to string.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsItselfInString() throws Exception {
        MatcherAssert.assertThat(
            new NoiseReduction(
                "some regular expression",
                1,
                Mockito.mock(Drain.class),
                Mockito.mock(Drain.class)
            ),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
