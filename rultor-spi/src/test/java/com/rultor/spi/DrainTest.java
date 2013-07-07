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
package com.rultor.spi;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Drain}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DrainTest {

    /**
     * Drain.Line.Simple can build a string.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesString() throws Exception {
        final long msec = Math.abs(new Random().nextLong());
        final Drain.Line line = new Drain.Line.Simple(
            msec, Level.INFO, "msg"
        );
        MatcherAssert.assertThat(
            line,
            Matchers.hasToString(Matchers.endsWith(" INFO msg"))
        );
        MatcherAssert.assertThat(
            Drain.Line.Simple.has(line.toString()),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            Drain.Line.Simple.parse(line.toString()).level(),
            Matchers.equalTo(Level.INFO)
        );
    }

    /**
     * Drain.Line.Simple can build dates correctly.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings({
        "PMD.AvoidInstantiatingObjectsInLoops",
        "PMD.AvoidUsingHardCodedIP"
    })
    public void buildsDatesCorrectly() throws Exception {
        final ImmutableMap<Long, String> map =
            new ImmutableMap.Builder<Long, String>()
                // @checkstyle MagicNumber (4 lines)
                .put(61000L, "1:01")
                .put(1000L, "0:01")
                .put(185000L, "3:05")
                .put(24005000L, "400:05")
                .build();
        for (Map.Entry<Long, String> entry : map.entrySet()) {
            final Drain.Line line = new Drain.Line.Simple(
                entry.getKey(), Level.INFO, "text msg"
            );
            MatcherAssert.assertThat(
                line.toString().trim(),
                Matchers.startsWith(entry.getValue())
            );
            MatcherAssert.assertThat(
                Drain.Line.Simple.parse(line.toString()).msec(),
                Matchers.equalTo(entry.getKey())
            );
        }
    }

}
