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

import com.google.common.collect.ImmutableMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MergedMaps}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
public final class MergedMapsTest {

    /**
     * MergedMaps contains keys of both producing maps.
     */
    @Test
    public void containsKeysOfBothMaps() {
        final String keya = "a";
        final String keyb = "b";
        final String keyc = "c";
        final ImmutableMap<String, String> first =
            new ImmutableMap.Builder<String, String>()
                .put(keya, "firstA")
                .put(keyb, "firstB")
                .build();
        final ImmutableMap<String, String> second =
            new ImmutableMap.Builder<String, String>()
                .put(keyb, "secondB")
                .put(keyc, "secondC")
                .build();
        final MergedMaps<String, String> map = new MergedMaps<String, String>(
            first, second
        );
        MatcherAssert.assertThat(map.containsKey(keya), Matchers.is(true));
        MatcherAssert.assertThat(map.containsKey(keyb), Matchers.is(true));
        MatcherAssert.assertThat(map.containsKey(keyc), Matchers.is(true));
    }

    /**
     * Values of second map have higher priority in MergedMaps.
     */
    @Test
    public void secondMapHasHigherPriority() {
        final String key = "e";
        final String val = "secondE";
        final ImmutableMap<String, String> first =
            new ImmutableMap.Builder<String, String>()
                .put(key, "firstE")
                .build();
        final ImmutableMap<String, String> second =
            new ImmutableMap.Builder<String, String>()
                .put(key, val)
                .build();
        final MergedMaps<String, String> map = new MergedMaps<String, String>(
            first, second
        );
        MatcherAssert.assertThat(map.get(key), Matchers.is(val));
    }

    /**
     * MergedMaps size equals to the size of producing maps keys union.
     */
    @Test
    public void size() {
        final String keyf = "f";
        final String keyg = "g";
        final String keyh = "h";
        final ImmutableMap<String, String> first =
            new ImmutableMap.Builder<String, String>()
                .put(keyf, "firstF")
                .put(keyg, "firstG")
                .build();
        final ImmutableMap<String, String> second =
            new ImmutableMap.Builder<String, String>()
                .put(keyg, "secondG")
                .put(keyh, "secondH")
                .build();
        final MergedMaps<String, String> map = new MergedMaps<String, String>(
            first, second
        );
        MatcherAssert.assertThat(
            map.size(),
            Matchers.is(com.jcabi.aspects.Tv.THREE)
        );
    }
}
