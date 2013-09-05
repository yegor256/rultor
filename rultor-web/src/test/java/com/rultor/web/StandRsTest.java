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
import com.rexsl.test.XhtmlMatchers;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XSLT;
import com.rultor.spi.Pageable;
import com.rultor.spi.Pulse;
import com.rultor.spi.Stand;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import java.io.IOException;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Test case for {@link StandRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class StandRsTest {

    /**
     * Pre-load test MANIFEST.MF.
     * @throws IOException If fails
     */
    @BeforeClass
    public static void manifests() throws IOException {
        Manifests.inject("Rultor-Revision", "12345");
    }

    /**
     * StandRs can fetch snapshot in HTML.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void fetchesSnapshotInHtml() throws Exception {
        final StandRs rest = new StandRs();
        final Stand stand = Mockito.mock(Stand.class);
        final URN owner = new URN("urn:test:1");
        Mockito.doReturn(owner).when(stand).owner();
        final Pageable<Pulse, String> pulses = Mockito.mock(Pageable.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        final String name = "some-pulse identifier";
        final Pulse pulse = Mockito.mock(Pulse.class);
        Mockito.doReturn(
            new Directives()
                .add("spec").set("some text").up()
                .add("tags").add("tag").add("label").set("tag label")
                .toString()
        ).when(pulse).xembly();
        Mockito.doReturn(Arrays.asList(pulse).iterator())
            .when(pulses).iterator();
        Mockito.doReturn(pulses).when(pulses).tail(name);
        final Users users = Mockito.mock(Users.class);
        final User user = Mockito.mock(User.class);
        Mockito.doReturn(owner).when(user).urn();
        Mockito.doReturn(user).when(users).get(Mockito.any(URN.class));
        Mockito.doReturn(stand).when(users).stand(Mockito.anyString());
        rest.setServletContext(
            new ServletContextMocker()
                .withAttribute(Users.class.getName(), users).mock()
        );
        rest.setHttpHeaders(new HttpHeadersMocker().mock());
        rest.setUriInfo(new UriInfoMocker().mock());
        MatcherAssert.assertThat(
            rest.fetch(name).getEntity().toString(),
            XhtmlMatchers.hasXPath("/div//xhtml:ul")
        );
    }

    /**
     * StandRs can process snapshot with XSLT.
     * @throws Exception If some problem inside
     */
    @Test
    public void processesSnapshotWithXslt() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new XSLT(
                    new Snapshot(
                        new Directives()
                            .add("start").set("2012-08-23T15:00:00Z").up()
                            .add("products").add("product")
                            .add("markdown").set("hello!")
                    ),
                    this.getClass().getResourceAsStream("fetch.xsl")
                ).dom()
            ),
            XhtmlMatchers.hasXPaths(
                "/xhtml:div",
                "//xhtml:ul"
            )
        );
    }

}
