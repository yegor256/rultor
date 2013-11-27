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
import com.rultor.repo.ClasspathRepo;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulse;
import com.rultor.spi.Pulses;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.Stand;
import com.rultor.spi.Tag;
import com.rultor.spi.Tags;
import com.rultor.spi.Users;
import com.rultor.tools.Time;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link com.rultor.web.IndexRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class IndexRsAnonymousTest {

    /**
     * IndexRs generates only public pulses for anonymous users.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings({ "unchecked", "PMD.CloseResource" })
    public void providesOnlyPublicPulsesForAnonymous() throws Exception {
        Manifests.inject("Rultor-Revision", "1");
        final Users users = Mockito.mock(Users.class);
        final String visible = "visible";
        final String invisible = "invisible";
        final String viurn = "urn:user:user1";
        final String inviurn = "urn:user:user2";
        final Pulses pulses = Mockito.mock(Pulses.class);
        final Iterator<Pulse> iterator = Arrays.asList(
            this.pulse(visible, viurn),
            this.pulse(invisible, inviurn)
        ).iterator();
        Mockito.when(pulses.iterator()).thenReturn(iterator);
        Mockito.when(users.flow()).thenReturn(pulses);
        this.stand(users, visible, true);
        this.stand(users, invisible, false);
        final IndexRs res = this.prepare(users);
        final Response response = res.index();
        final String coords = "/page/pulses/pulse/coordinates[owner='%s']";
        MatcherAssert.assertThat(
            JaxbConverter.the(response.getEntity()),
            XhtmlMatchers.hasXPaths(String.format(coords, viurn))
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(response.getEntity()),
            Matchers.not(
                XhtmlMatchers.hasXPaths(String.format(coords, inviurn))
            )
        );
    }

    /**
     * Prepare IndexRs to be used in tests.
     * @param users Users to store in attributes.
     * @return IndexRs with mocked data.
     */
    private IndexRs prepare(final Users users) {
        final IndexRs res = new IndexRs();
        res.setUriInfo(new UriInfoMocker().mock());
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setSecurityContext(Mockito.mock(SecurityContext.class));
        res.setServletContext(
            new ServletContextMocker()
                .withAttribute(Users.class.getName(), users)
                .withAttribute(Repo.class.getName(), new ClasspathRepo())
                .mock()
        );
        return res;
    }

    /**
     * Mock pulse.
     * @param stand Stand name.
     * @param urn User URN.
     * @return Pulse
     */
    private Pulse pulse(final String stand, final String urn) {
        final Pulse pulse = Mockito.mock(Pulse.class);
        final Coordinates coords = Mockito.mock(Coordinates.class);
        Mockito.when(coords.owner()).thenReturn(URN.create(urn));
        Mockito.when(coords.rule()).thenReturn("");
        Mockito.when(coords.scheduled()).thenReturn(new Time());
        Mockito.when(pulse.coordinates()).thenReturn(coords);
        Mockito.when(pulse.stand()).thenReturn(stand);
        final Tags tags = Mockito.mock(Tags.class);
        Mockito.when(tags.iterator())
            .thenReturn(Collections.<Tag>emptyIterator());
        Mockito.when(pulse.tags()).thenReturn(tags);
        return pulse;
    }

    /**
     * Add a mock stand.
     * @param users Users that provide stands.
     * @param name Name of the stand.
     * @param visible If the stand is public.
     */
    private void stand(final Users users, final String name,
        final boolean visible) {
        final Stand stand = Mockito.mock(Stand.class);
        final Spec acl;
        if (visible) {
            acl = new Spec.Simple("com.rultor.acl.OpenView()");
        } else {
            acl = new Spec.Simple("com.rultor.acl.Prohibited()");
        }
        Mockito.when(stand.acl()).thenReturn(acl);
        Mockito.when(users.stand(name)).thenReturn(stand);
    }
}
