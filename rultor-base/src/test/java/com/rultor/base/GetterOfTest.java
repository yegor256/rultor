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

import com.rultor.base.test.InvokerTestHelper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link GetterOf}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class GetterOfTest {
    /**
     * Correct bean class with property getter.
     */
    private static final class Correct {
        /**
         * Arbitrary number.
         */
        private final transient Integer number;

        /**
         * Arbitrary names.
         */
        private final transient String names;

        /**
         * Constructor.
         * @param num Number to store.
         * @param nam Name to store
         */
        private Correct(final Integer num, final String nam) {
            this.number = num;
            this.names = nam;
        }

        /**
         * Getter for number.
         * @return Stored number.
         */
        public Integer getNumber() {
            return this.number;
        }

        /**
         * Getter for name.
         * @return Stored name.
         */
        public String txt() {
            return this.names;
        }

        /**
         * Simple method.
         * @param num Number.
         * @return Stored name.
         */
        public String nameParams(final int num) {
            return this.names;
        }
    }

    /**
     * Correct call.
     */
    @Test
    public void correct() {
        final Integer value = 1;
        MatcherAssert.assertThat(
            new GetterOf(new GetterOfTest.Correct(value, ""), "number")
                .object(),
            Matchers.is((Object) value)
        );
    }

    /**
     * Correct call for anonymous from other package.
     */
    @Test
    public void anonymous() {
        final String value = "val";
        MatcherAssert.assertThat(
            new GetterOf(new InvokerTestHelper().make(value), "name")
                .object(),
            Matchers.is((Object) value)
        );
    }

    /**
     * Test for short format of getter.
     */
    @Test
    public void shortGetter() {
        final String value = "short";
        MatcherAssert.assertThat(
            new GetterOf(new GetterOfTest.Correct(0, value), "txt").object(),
            Matchers.is((Object) value)
        );
    }

    /**
     * Call with wrong property name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidName() {
        new GetterOf(new GetterOfTest.Correct(1, ""), "wrong").object();
    }

    /**
     * Call a method, not a getter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void methodWithParams() {
        new GetterOf(new GetterOfTest.Correct(1, ""), "nameParams").object();
    }
}
