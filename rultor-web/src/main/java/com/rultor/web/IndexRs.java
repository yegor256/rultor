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
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.auth.Identity;
import com.rultor.users.Unit;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/")
@Loggable(Loggable.DEBUG)
public final class IndexRs extends BaseRs {

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/")
    public Response index() throws Exception {
        final Identity self = this.auth().identity();
        Response response;
        if (self.equals(Identity.ANONYMOUS)) {
            response = new PageBuilder()
                .stylesheet("/xsl/front.xsl")
                .build(EmptyPage.class)
                .init(this)
                .render()
                .build();
        } else {
            response = new PageBuilder()
                .stylesheet("/xsl/index.xsl")
                .build(EmptyPage.class)
                .init(this)
                .append(this.mine())
                .link(new Link("add", "/u/add"))
                .render()
                .build();
        }
        return response;
    }

    /**
     * All my units.
     * @return Collection of JAXB units
     */
    private JaxbBundle mine() {
        return new JaxbBundle("units").add(
            new JaxbBundle.Group<Map.Entry<String, Unit>>(
                this.user().units().entrySet()) {
                @Override
                public JaxbBundle bundle(
                    final Map.Entry<String, Unit> entry) {
                    return IndexRs.this.unit(
                        entry.getKey(), entry.getValue()
                    );
                }
            }
        );
    }

    /**
     * Convert unit to JaxbBundle.
     * @param name Name of it
     * @param unit The unit
     * @return Bundle
     */
    private JaxbBundle unit(final String name, final Unit unit) {
        return new JaxbBundle("unit")
            .add("name", name)
            .up()
            .add("spec", unit.spec().asText())
            .up()
            .link(
                new Link(
                    // @checkstyle MultipleStringLiterals (1 line)
                    "remove",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(UnitRs.class)
                        .path(UnitRs.class, "remove")
                        .queryParam(UnitRs.QUERY_NAME, "{n1}")
                        .build(name)
                )
            )
            .link(
                new Link(
                    "edit",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(UnitRs.class)
                        .queryParam(UnitRs.QUERY_NAME, "{n2}")
                        .build(name)
                )
            )
            .link(
                new Link(
                    "pulses",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(PulsesRs.class)
                        .queryParam(PulsesRs.QUERY_NAME, "{n7}")
                        .build(name)
                )
            );
    }

}
