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

import com.rultor.spi.Drain;
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
    @SuppressWarnings("unchecked")
    public void filtersNoise() throws Exception {
        final Drain dirty = Mockito.mock(Drain.class);
        final Drain clean = Mockito.mock(Drain.class);
        final Drain drain = new NoiseReduction(
            new Work.Simple(),
            ".*Hello[0-9]+.*",
            1,
            dirty,
            clean
        );
        final String bad = "somefffffds900-4932%^&$%^&#%@^&!\u20ac\tfdsfs";
        final String good = "how about this? Hello1!";
        drain.append(Arrays.asList(bad));
        Mockito.verify(dirty, Mockito.times(1))
            .append(Mockito.any(Iterable.class));
        Mockito.doReturn(
            IOUtils.toInputStream(
                String.format("%s\n%s\n", bad, good),
                CharEncoding.UTF_8
            )
        ).when(dirty).read();
        drain.append(Arrays.asList(good));
        Mockito.verify(clean, Mockito.times(1)).append(
            Mockito.argThat(Matchers.hasItems(bad, good))
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
                new Work.Simple(),
                "some regular expression",
                1,
                Mockito.mock(Drain.class),
                Mockito.mock(Drain.class)
            ),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
