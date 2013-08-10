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
package com.rultor.snapshot;

import com.jcabi.aspects.Tv;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link StepAspect}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class StepAspectTest {

    /**
     * StepAspect can report progress of a method.
     * @throws Exception If some problem inside
     */
    @Test
    public void reportsMethodProgressInLog() throws Exception {
        MatcherAssert.assertThat(
            new StepAspectTest.Foo("0x").toHex(Tv.HUNDRED),
            Matchers.equalTo("0x64")
        );
    }

    /**
     * Dummy class for testing.
     */
    private static final class Foo {
        /**
         * Some property encapsulated.
         */
        private final transient String prefix;
        /**
         * Ctor.
         * @param pfx Prefix
         */
        protected Foo(final String pfx) {
            this.prefix = pfx;
        }
        /**
         * Simple method.
         * @param number Number to convert to hex
         * @return Hexadecimal number
         */
        @Step(
            before = "planning to do something with ${args[0]}",
            value = "returned `${result}` on ${args[0]} with `${this.prefix}`"
        )
        public String toHex(final int number) {
            return String.format(
                "%s%s",
                this.prefix, Integer.toHexString(number)
            );
        }
    }

}
