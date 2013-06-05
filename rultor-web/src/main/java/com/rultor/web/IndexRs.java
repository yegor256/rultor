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
import java.util.logging.Level;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
public final class IndexRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_NAME = "name";

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/")
    @Loggable(Loggable.DEBUG)
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
                .link(new Link("add", "./add"))
                .render()
                .build();
        }
        return response;
    }

    /**
     * Add new unit (front page).
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/add")
    @Loggable(Loggable.DEBUG)
    public Response add() throws Exception {
        return new PageBuilder()
            .stylesheet("/xsl/add.xsl")
            .build(EmptyPage.class)
            .init(this)
            .link(new Link("save", "./save"))
            .render()
            .build();
    }

    /**
     * Save new or existing unit unit.
     * @param name Name of it
     * @param spec Spec to save
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @POST
    @Path("/save")
    @Loggable(Loggable.DEBUG)
    public Response save(@FormParam("name") final String name,
        @FormParam("spec") final String spec) throws Exception {
        Unit unit = this.user().units().get(spec);
        if (unit == null) {
            unit = this.user().create(name);
        }
        unit.spec(this.repo().make(spec));
        throw this.flash().redirect(
            this.uriInfo().getBaseUriBuilder()
                .clone()
                .path(IndexRs.class)
                .build(),
            String.format(
                "Unit '%d' successfully saved/updated",
                name
            ),
            Level.INFO
        );
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
            .add("urn", unit.urn().toString())
            .up()
            .link(
                new Link(
                    "remove",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(IndexRs.class)
                        .path(IndexRs.class, "remove")
                        .queryParam(IndexRs.QUERY_NAME, "{n1}")
                        .build(name)
                )
            )
            .link(
                new Link(
                    "edit",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(UnitRs.class)
                        .path(UnitRs.class, "edit")
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
                        .path(PulsesRs.class, "front")
                        .queryParam(PulsesRs.QUERY_URN, "{n7}")
                        .build(unit.urn())
                )
            );
    }

}
