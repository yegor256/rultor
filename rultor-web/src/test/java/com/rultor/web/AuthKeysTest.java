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
package com.rultor.web;

import com.rexsl.page.auth.Identity;
import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link AuthKeys}.
 * @author Shailendra Soni (soni_shailendra02@yahoo.com)
 * @version $Id$
 */
public class AuthKeysTest {

    /**
     * Validate weather user is null or not.
     */
    @Test(expected = ConstraintViolationException.class)
    public final void validateNullUser() {
        final AuthKeys authKeys = new AuthKeys();
        final String user = null;
        final String password = "password";
        authKeys.authenticate(user, password);
    }

    /**
     * It should be throw exception if password is given null.
     */
    @Test(expected = ConstraintViolationException.class)
    public final void validateNullPassword() {
        final AuthKeys authKeys = new AuthKeys();
        final String user = "urn:git:soni";
        final String password = null;
        authKeys.authenticate(user, password);
    }

    /**
     * Giving proper data to authenticate.
     */
    @Test
    public final void authenticateURN() {
        final AuthKeys authKeys = new AuthKeys();
        final String user = "urn:git:rultor";
        final String password = "test";
        MatcherAssert.assertThat(
            authKeys.authenticate(user, password)
                .name(),
                Matchers.equalTo(Identity.ANONYMOUS.name())
        );
    }

    /**
     * Execute method with Empty String.
     * @TODO #123?,Ideally target class AuthKeys should also have
     *  validation of empty string.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void authenticateURNWithEmptyUser() {
        final AuthKeys authKeys = new AuthKeys();
        final String user = "";
        final String password = "";
        MatcherAssert.assertThat(
            authKeys.authenticate(user, password)
                .name(),
                Matchers.equalTo(Identity.ANONYMOUS.name())
        );
    }

}
