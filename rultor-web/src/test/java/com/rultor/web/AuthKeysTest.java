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
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link AuthKeys}.
 * @author Shailendra Soni (soni_shailendra02@yahoo.com)
 * @version $Id$
 */
public class AuthKeysTest {

    /**
     * Validate weather user is null or not.
     * @throws Exception If test fails
     */
    @Test(expected = ConstraintViolationException.class)
    public final void validateNullUser() throws Exception {
        final AuthKeys authKeys = new AuthKeys();
        authKeys.authenticate(null, "password");
    }

    /**
     * It should be throw exception if password is given null.
     * @throws Exception If test fails
     */
    @Test(expected = ConstraintViolationException.class)
    public final void validateNullPassword() throws Exception {
        final AuthKeys authKeys = new AuthKeys();
        authKeys.authenticate("urn:git:soni", null);
    }

    /**
     * AuthKeys should authenticate URN.
     * @throws Exception If test fails
     */
    @Test
    public final void authenticateURN() throws Exception {
        final AuthKeys authKeys = new AuthKeys();
        MatcherAssert.assertThat(
            authKeys.authenticate("urn:git:rultor", "test").name(),
            Matchers.equalTo(Identity.ANONYMOUS.name())
        );
    }

    /**
     * Execute method with Empty String.
     * @throws Exception If test fails
     * @todo #238 AuthKeys should validate empty string.
     */
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public final void authenticateURNWithEmptyUser() throws Exception {
        final AuthKeys authKeys = new AuthKeys();
        MatcherAssert.assertThat(
            authKeys.authenticate("", "").name(),
            Matchers.equalTo(Identity.ANONYMOUS.name())
        );
    }

}
