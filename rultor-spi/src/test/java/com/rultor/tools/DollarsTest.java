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
package com.rultor.tools;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Tv;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Dollars}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DollarsTest {

    /**
     * Dollars can print monetary value.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsItselfToString() throws Exception {
        final ImmutableMap<Dollars, String> map =
            new ImmutableMap.Builder<Dollars, String>()
                .put(new Dollars(0L), "$0.00")
                .put(new Dollars(Tv.MILLION), "$1.00")
                .put(new Dollars(Tv.FIVE * Tv.MILLION), "$5.00")
                .put(new Dollars(Tv.THOUSAND), "$0.001")
                .put(new Dollars(Tv.TEN * Tv.THOUSAND), "$0.01")
                .put(new Dollars(-Tv.FIVE * Tv.MILLION), "($5.00)")
                .build();
        for (Map.Entry<Dollars, String> entry : map.entrySet()) {
            MatcherAssert.assertThat(
                entry.getKey(),
                Matchers.hasToString(entry.getValue())
            );
            MatcherAssert.assertThat(
                Dollars.valueOf(entry.getValue()),
                Matchers.equalTo(entry.getKey())
            );
        }
    }

}
