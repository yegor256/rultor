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
import org.apache.velocity.exception.VelocityException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Vext}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class VextTest {

    /**
     * Vext can print with arguments.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsWithArguments() throws Exception {
        final ImmutableMap<String, Object> map =
            new ImmutableMap.Builder<String, Object>()
                .put("foo", "$1.00")
                .build();
        MatcherAssert.assertThat(
            new Vext("cookie costs ${foo}").print(map),
            Matchers.equalTo("cookie costs $1.00")
        );
    }

    /**
     * Vext#print throws informative exceptions.
     */
    @Test
    public void printThrowsInformativeExceptions() {
        final String text = "#a(-+uuu)";
        try {
            new Vext(text).print(
                new ImmutableMap.Builder<String, Object>().build()
            );
        } catch (VelocityException ex) {
            MatcherAssert.assertThat(
                ex.getMessage(),
                Matchers.containsString(text)
            );
        }
    }
}
