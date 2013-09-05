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

import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rexsl.page.HttpHeadersMocker;
import com.rexsl.page.ServletContextMocker;
import com.rexsl.page.UriInfoMocker;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import com.rultor.spi.Account;
import com.rultor.spi.Rule;
import com.rultor.spi.Rules;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.tools.Dollars;
import java.io.IOException;
import java.util.ArrayList;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link IndexRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class IndexRsTest {

    /**
     * Pre-load test MANIFEST.MF.
     * @throws IOException If fails
     */
    @Before
    public void manifests() throws IOException {
        Manifests.inject("Rultor-Revision", "12345");
    }

    /**
     * IndexRs can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings({ "unchecked", "PMD.CloseResource" })
    public void rendersFrontPage() throws Exception {
        final IndexRs res = new IndexRs();
        res.setUriInfo(new UriInfoMocker().mock());
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setSecurityContext(Mockito.mock(SecurityContext.class));
        final User user = Mockito.mock(User.class);
        final Rules rules = Mockito.mock(Rules.class);
        Mockito.doReturn(rules).when(user).rules();
        Mockito.doReturn(new ArrayList<Rule>(0).iterator())
            .when(rules).iterator();
        final Users users = Mockito.mock(Users.class);
        Mockito.doReturn(user).when(users).get(Mockito.any(URN.class));
        final Account account = Mockito.mock(Account.class);
        Mockito.doReturn(account).when(user).account();
        Mockito.doReturn(new Dollars(1)).when(account).balance();
        res.setServletContext(
            new ServletContextMocker().withAttribute(
                Users.class.getName(), users
            ).mock()
        );
        final Response response = res.index();
        MatcherAssert.assertThat(
            JaxbConverter.the(response.getEntity()),
            XhtmlMatchers.hasXPaths(
                "/page/version[name='1.0-SNAPSHOT']"
            )
        );
    }

}
