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
import com.rultor.users.Unit;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Pulses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/u")
@Loggable(Loggable.DEBUG)
public final class UnitRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_NAME = "name";

    /**
     * View and edit existing unit (front page).
     * @param name Name of the unit
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/")
    public Response edit(@QueryParam("name") @NotNull final String name)
        throws Exception {
        final Unit unit = this.user().units().get(name);
        if (unit == null) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUriBuilder()
                    .clone()
                    .path(IndexRs.class)
                    .build(),
                String.format(
                    "Unit '%s' not found",
                    name
                ),
                Level.SEVERE
            );
        }
        return new PageBuilder()
            .stylesheet("/xsl/add.xsl")
            .build(EmptyPage.class)
            .init(this)
            .link(new Link("save", "./save"))
            .append(
                new JaxbBundle("unit")
                    .add("name", name)
                    .up()
                    .add("spec", unit.spec().asText())
                    .up()
            )
            .render()
            .build();
    }

    /**
     * Remove unit by name.
     * @param name Name of the unit to remove
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/remove")
    public Response remove(@QueryParam(UnitRs.QUERY_NAME)
        @NotNull final String name) throws Exception {
        this.user().remove(name);
        throw this.flash().redirect(
            this.uriInfo().getBaseUriBuilder()
                .clone()
                .path(IndexRs.class)
                .build(),
            String.format(
                "Unit '%s' successfully removed",
                name
            ),
            Level.INFO
        );
    }

    /**
     * Add new unit (front page).
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/add")
    public Response add() throws Exception {
        return new PageBuilder()
            .stylesheet("/xsl/add.xsl")
            .build(EmptyPage.class)
            .init(this)
            .link(new Link("save", "/u/save"))
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
    public Response save(@FormParam("name") final String name,
        @NotNull @FormParam("spec") final String spec) throws Exception {
        Unit unit = this.user().units().get(name);
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
                "Unit '%s' successfully saved/updated",
                name
            ),
            Level.INFO
        );
    }

}
