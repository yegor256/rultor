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

import java.util.concurrent.ConcurrentHashMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SecretMap}.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
public final class SecretMapTest {

    /**
     * SecretMap can do basic operations.
     */
    @Test
    public void basicOperations() {
        final ConcurrentHashMap<String, Object> map =
            new ConcurrentHashMap<String, Object>(2);
        final SecretMap secretmap = new SecretMap(map);
        MatcherAssert.assertThat(secretmap.isEmpty(), Matchers.is(true));
        MatcherAssert.assertThat(secretmap.size(), Matchers.is(0));
        MatcherAssert.assertThat(secretmap.get("a"), Matchers.nullValue());
        MatcherAssert.assertThat(
            secretmap.containsKey("b"), Matchers.is(false)
        );
        final String value = "1";
        map.put("c", value);
        final SecretMap smap = new SecretMap(map);
        MatcherAssert.assertThat(smap.isEmpty(), Matchers.is(false));
        MatcherAssert.assertThat(smap.size(), Matchers.is(1));
        MatcherAssert.assertThat(
            smap.containsValue(value), Matchers.is(true)
        );
    }

    /**
     * SecretMap toString prints how many pairs it has.
     */
    @Test
    public void canPrintBasedOnsize() {
        final ConcurrentHashMap<String, Object> map =
            new ConcurrentHashMap<String, Object>(2);
        final SecretMap secretmap = new SecretMap(map);
        MatcherAssert.assertThat(secretmap.toString(), Matchers.is("{}"));
        map.put("d", "2");
        final SecretMap smap = new SecretMap(map);
        MatcherAssert.assertThat(
            smap.toString(), Matchers.is("{1 pair(s)}")
        );
        map.put("e", "3");
        final SecretMap scmap = new SecretMap(map);
        MatcherAssert.assertThat(
            scmap.toString(), Matchers.is("{2 pair(s)}")
        );
    }
}
