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
import java.util.concurrent.ConcurrentHashMap;
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
     * MergedMaps can do basic operations.
     */
    @Test
    public void basicOperations() {
        final MergedMaps<String, String> empty = new MergedMaps<String, String>(
            new ConcurrentHashMap<String, String>(),
            new ConcurrentHashMap<String, String>()
        );
        MatcherAssert.assertThat(empty.isEmpty(), Matchers.is(true));
        MatcherAssert.assertThat(empty.containsKey("f"), Matchers.is(false));
        MatcherAssert.assertThat(empty.size(), Matchers.is(0));
        final ImmutableMap<String, String> first =
            new ImmutableMap.Builder<String, String>()
                // @checkstyle MultipleStringLiterals (8 line)
                .put("a", "firstA")
                .put("b", "firstB")
                .build();
        final ImmutableMap<String, String> second =
            new ImmutableMap.Builder<String, String>()
                .put("b", "secondB")
                .put("c", "secondC")
                .build();
        final MergedMaps<String, String> map = new MergedMaps<String, String>(
            first,
            second);
        MatcherAssert.assertThat(map.get("a"), Matchers.is("firstA"));
        MatcherAssert.assertThat(map.get("b"), Matchers.is("secondB"));
        MatcherAssert.assertThat(map.get("c"), Matchers.is("secondC"));
        MatcherAssert.assertThat(map.isEmpty(), Matchers.is(false));
        // @checkstyle MagicNumberCheck (1 line)
        MatcherAssert.assertThat(map.size(), Matchers.is(3));
    }
}
