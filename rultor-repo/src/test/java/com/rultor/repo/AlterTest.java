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
package com.rultor.repo;

import com.jcabi.urn.URN;
import com.rultor.spi.Arguments;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Wallet;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Sharp}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class AlterTest {

    /**
     * Alter can make an instance.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesInstance() throws Exception {
        final String text = "\u20ac \"' ${work.rule()}";
        final Variable<String> var = new Alter(text);
        MatcherAssert.assertThat(
            var.instantiate(
                Mockito.mock(Users.class),
                new Arguments(
                    new Coordinates.Simple(new URN("urn:test:1"), "hey"),
                    new Wallet.Empty()
                )
            ),
            Matchers.equalTo("\u20ac \"' hey")
        );
    }

    /**
     * Alter can make an instance with arguments.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesInstanceWithArguments() throws Exception {
        final String text = "${work.rule()}: #arg(0,'this is test')";
        final Variable<String> var = new Alter(text);
        MatcherAssert.assertThat(
            var.instantiate(
                Mockito.mock(Users.class),
                new Arguments(
                    new Coordinates.Simple(new URN("urn:test:5"), "r"),
                    new Wallet.Empty()
                ).with(0, "hello-\u20ac")
            ),
            Matchers.equalTo("r: hello-\u20ac")
        );
    }

    /**
     * Alter can detect arguments.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsArguments() throws Exception {
        final String text = "hello: #arg(0, 'hey you \u20ac')boom #arg(2,'')";
        final Variable<String> var = new Alter(text);
        MatcherAssert.assertThat(
            var.arguments(),
            Matchers.allOf(
                Matchers.hasEntry(
                    Matchers.equalTo(0),
                    Matchers.equalTo("hey you \u20ac")
                ),
                Matchers.hasEntry(
                    Matchers.equalTo(2),
                    Matchers.equalTo("")
                )
            )
        );
    }

}
