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

import com.google.common.io.Files;
import com.rultor.drain.files.DirectoryDrain;
import com.rultor.spi.Drain;
import com.rultor.spi.Time;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link NoiseReductionDrain}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class NoiseReductionDrainTest {

    /**
     * NoiseReductionDrain can filter.
     * @throws Exception If some problem inside
     */
    @Test
    public void filtersNoise() throws Exception {
        final Drain dirty = new DirectoryDrain(Files.createTempDir());
        final Drain clean = new DirectoryDrain(Files.createTempDir());
        final Drain drain = new NoiseReductionDrain(
            "Hello[0-9]+",
            1,
            dirty,
            clean
        );
        final Time first = new Time(Math.abs(new Random().nextLong()));
        final Time second = new Time(Math.abs(new Random().nextLong()));
        final String good = "some \t\u20ac\tfdsfs Hello878";
        final String bad = "somefffffds900-4932%^&$%^&#%@^&!\u20ac\tfdsfs";
        drain.append(first, Arrays.asList(bad));
        drain.append(first, Arrays.asList(good));
        drain.append(second, Arrays.asList(bad));
        drain.append(second, Arrays.asList(bad));
        MatcherAssert.assertThat(
            drain.pulses(),
            Matchers.<Time>iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            dirty.pulses(),
            Matchers.allOf(
                Matchers.<Time>iterableWithSize(2),
                Matchers.hasItems(first, second)
            )
        );
        MatcherAssert.assertThat(
            clean.pulses(),
            Matchers.allOf(
                Matchers.<Time>iterableWithSize(1),
                Matchers.hasItem(first)
            )
        );
        MatcherAssert.assertThat(
            IOUtils.toString(drain.read(first), CharEncoding.UTF_8),
            Matchers.containsString(good)
        );
        MatcherAssert.assertThat(
            IOUtils.toString(drain.read(second), CharEncoding.UTF_8),
            Matchers.containsString(bad)
        );
    }

}
