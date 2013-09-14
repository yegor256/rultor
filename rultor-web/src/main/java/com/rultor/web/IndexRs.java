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

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.auth.Identity;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tag;
import java.util.Iterator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/")
@Loggable(Loggable.DEBUG)
public final class IndexRs extends BaseRs {

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        final Identity self = this.auth().identity();
        final Response response;
        if (self.equals(Identity.ANONYMOUS)) {
            response = new PageBuilder()
                .stylesheet("/xsl/front.xsl")
                .build(EmptyPage.class)
                .init(this)
                .append(new Breadcrumbs().with("self", "home").bundle())
                .append(this.pulses(this.users().flow().iterator(), Tv.TWENTY))
                .render()
                .build();
        } else {
            response = new PageBuilder()
                .stylesheet("/xsl/index.xsl")
                .build(EmptyPage.class)
                .init(this)
                .append(new Breadcrumbs().with("self", "home").bundle())
                .append(
                    this.pulses(
                        this.user().stands().flow().iterator(), Tv.TWENTY
                    )
                )
                .render()
                .build();
        }
        return response;
    }

    /**
     * All pulses of the stand.
     * @param pulses All pulses to show
     * @param maximum Maximum to show
     * @return Collection of JAXB stands
     */
    private JaxbBundle pulses(final Iterator<Pulse> pulses, final int maximum) {
        JaxbBundle bundle = new JaxbBundle("pulses");
        int pos;
        for (pos = 0; pos < maximum; ++pos) {
            if (!pulses.hasNext()) {
                break;
            }
            bundle = bundle.add(this.pulse(pulses.next()));
        }
        return bundle;
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param pulse The pulse
     * @return Bundle
     */
    private JaxbBundle pulse(final Pulse pulse) {
        final Coordinates coords = pulse.coordinates();
        final String label = new Coordinates.Simple(coords).toString();
        return new JaxbBundle("pulse")
            .add("coordinates")
            .add("rule", coords.rule()).up()
            .add("owner", coords.owner().toString()).up()
            .add("scheduled", coords.scheduled().toString()).up()
            .up()
            .link(
                new Link(
                    "open",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(StandRs.class)
                        .queryParam(StandRs.QUERY_OPEN, "{label}")
                        .build(pulse.stand(), label)
                )
            )
            .add("tags")
            .add(
                new JaxbBundle.Group<Tag>(pulse.tags()) {
                    @Override
                    public JaxbBundle bundle(final Tag tag) {
                        return new JaxbBundle("tag")
                            .add("label", tag.label()).up()
                            .add("level", tag.level().toString()).up()
                            .add("markdown", tag.markdown()).up();
                    }
                }
            )
            .up();
    }

}
