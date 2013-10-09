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

import java.io.IOException;
import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.jcabi.manifests.Manifests;
import com.rexsl.page.auth.Identity;

/**
 * Test case for {@link AuthKeys}.
 * @author Shailendra Soni
 * @version $Id$
 */

public class AuthKeysTest {

	/**
	 * Pre-load test MANIFEST.MF.
	 * @throws IOExceptionIf fails
	 */
	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Before
	public void manifests() throws IOException {
		Manifests.inject("Rultor-Revision", "12345");
	}

	/**
	 * Validate weather user is null or not.
	 */
	@Test
	public void validateNullUser() {
		thrown.expect(ConstraintViolationException.class);
		final AuthKeys authKeys = new AuthKeys();
		final String user = null;
		final String password = "test";
		authKeys.authenticate(user, password);
	}

	/**
	 * It should be throw exception if password is given null.
	 */
	@Test
	public void validateNullPassword() {
		thrown.expect(ConstraintViolationException.class);
		final AuthKeys authKeys = new AuthKeys();
		final String user = "urn:git:test";
		final String password = null;
		authKeys.authenticate(user, password);
	}

	/**
	 * Giving proper data to authenticate.
	 */
	@Test
	public void authenticateURN() {
		final AuthKeys authKeys = new AuthKeys();
		final String user = "urn:git:test";
		final String password = "test";
		MatcherAssert.assertThat(authKeys.authenticate(user, password).name(),
		        Matchers.equalTo(Identity.ANONYMOUS.name()));
	}

	
	/**
	 * @TODO #123? , Ideally target class AuthKeys should also have validation of empty string.
	 */
	@Test
	public void authenticateURNWithEmptyUser() {
		thrown.expect(IllegalArgumentException.class);
		final AuthKeys authKeys = new AuthKeys();
		final String user = "";
		final String password = "";
		MatcherAssert.assertThat(authKeys.authenticate(user, password).name(),
		        Matchers.equalTo(Identity.ANONYMOUS.name()));
	}

}
