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
import com.rexsl.page.inset.FlashInset;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.SpecException;
import com.rultor.spi.Unit;
import com.rultor.spi.Variable;
import com.rultor.spi.Work;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Pulses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/unit/{name:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
public final class UnitRs extends BaseRs {

    /**
     * Unit name.
     */
    private transient String name;

    /**
     * Inject it from query.
     * @param unit Unit name (or NULL)
     */
    @PathParam("name")
    public void setName(@NotNull final String unit) {
        this.name = unit;
    }

    /**
     * View an existing unit or an empty one.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return this.head()
            .append(
                new JaxbBundle("unit")
                    .add("name", this.name)
                    .up()
                    .add("spec", this.unit().spec().asText())
                    .up()
            )
            .render()
            .build();
    }

    /**
     * Remove unit by name.
     * @return The JAX-RS response
     */
    @GET
    @Path("/remove")
    public Response remove() {
        this.user().remove(this.name);
        throw this.flash().redirect(
            this.uriInfo().getBaseUri(),
            String.format("Unit '%s' successfully removed", this.name),
            Level.INFO
        );
    }

    /**
     * Save new or existing unit unit.
     * @param spec Spec to save
     * @return The JAX-RS response
     */
    @POST
    @Path("/")
    public Response save(@NotNull @FormParam("spec") final String spec) {
        try {
            this.unit().update(this.parse(spec, Object.class));
        } catch (SpecException ex) {
            return this.head()
                .append(FlashInset.bundle(Level.SEVERE, ex.getMessage(), 0L))
                .append(
                    new JaxbBundle("unit")
                        .add("name", this.name)
                        .up()
                        .add("spec", spec)
                        .up()
                )
                .render()
                .status(HttpURLConnection.HTTP_BAD_REQUEST)
                .build();
        }
        throw this.flash().redirect(
            this.uriInfo().getRequestUri(),
            String.format("Unit '%s' successfully saved/updated", this.name),
            Level.INFO
        );
    }

    /**
     * Get Unit.
     * @return Unit
     */
    private Unit unit() {
        if (!this.user().units().contains(this.name)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("Unit '%s' doesn't exist", this.name),
                Level.SEVERE
            );
        }
        return this.user().get(this.name);
    }

    /**
     * Make a spec from text.
     * @param text Text
     * @param type Expected type of it
     * @return Spec
     * @throws SpecException If invalid input
     */
    private Spec parse(final String text, final Class<?> type)
        throws SpecException {
        final Variable<?> var = new Repo.Cached(
            this.repo(), this.user(), new Spec.Simple(text)
        ).get();
        final Object object = var.instantiate(this.users(), new Work.None());
        try {
            object.toString();
        } catch (SecurityException ex) {
            throw new SpecException(ex);
        }
        if (!type.isAssignableFrom(object.getClass())) {
            throw new SpecException(
                String.format(
                    "%s expected while %s provided",
                    type.getName(),
                    object.getClass().getName()
                )
            );
        }
        return new Spec.Simple(var.asText());
    }

    /**
     * Make a head of the page.
     * @return The page
     */
    private EmptyPage head() {
        return new PageBuilder()
            .stylesheet("/xsl/unit.xsl")
            .build(EmptyPage.class)
            .init(this)
            .link(
                new Link(
                    "save",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(UnitRs.class)
                        .path(UnitRs.class, "save")
                        .build(this.name)
                )
            )
            .link(
                new Link(
                    "remove",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(UnitRs.class)
                        .path(UnitRs.class, "remove")
                        .build(this.name)
                )
            );
    }

}
