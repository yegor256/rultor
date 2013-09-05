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
package com.rultor.env;

import java.net.InetAddress;
import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Server}.
 * @author Gangababu Tirumalanadhuni (gangababu.t@gmail.com)
 * @version $Id$
 */
public final class ServerTest {

    /**
     * Server IP can't be NULL.
     * @throws Exception If some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void serverIPCantBeNull() throws Exception {
        new Server(null);
    }

    /**
     * Get new environment.
     * @throws Exception If some problem inside
     */
    @Test
    public void testAcquire() throws Exception {
        final Environment env = Mockito.mock(Environment.class);
        final InetAddress address = Mockito.mock(InetAddress.class);
        final Environments server = Mockito.mock(Environments.class);
        Mockito.doReturn(env).when(server).acquire();
        Mockito.doReturn(address).when(env).address();
        MatcherAssert.assertThat(
            env, Matchers.equalTo(server.acquire())
        );
        MatcherAssert.assertThat(
            address, Matchers.equalTo(env.address())
        );
        Mockito.verify(env, Mockito.times(1)).address();
    }
}
