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
import com.rultor.spi.ACL;
import com.rultor.spi.Spec;
import com.rultor.spi.SpecException;
import com.rultor.spi.Stand;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import java.net.HttpURLConnection;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Edit ACL of stand.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/acl/{name:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
public final class AclRs extends BaseRs {

    /**
     * Stand name.
     */
    private transient String name;

    /**
     * Inject it from query.
     * @param stand Stand name (or NULL)
     */
    @PathParam("name")
    public void setName(@NotNull(message = "stand name is mandatory")
        final String stand) {
        this.name = stand;
    }

    /**
     * View an existing stand or an empty one.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return this.head()
            .append(
                new JaxbBundle("stand")
                    .add("name", this.name)
                    .up()
                    .add("acl", this.stand().acl().asText())
                    .up()
            )
            .render()
            .build();
    }

    /**
     * Save new or existing stand stand.
     * @param spec Spec to save
     * @return The JAX-RS response
     */
    @POST
    @Path("/")
    public Response save(@NotNull(message = "spec form param is mandatory")
        @FormParam("spec") final String spec) {
        try {
            this.stand().acl(
                new Spec.Strict(
                    spec, this.repo(), this.user(), this.users(),
                    new Work.None(), ACL.class
                )
            );
        } catch (SpecException ex) {
            return this.head()
                .append(FlashInset.bundle(Level.SEVERE, Exceptions.message(ex)))
                .append(
                    new JaxbBundle("stand")
                        .add("name", this.name)
                        .up()
                        .add("acl", spec)
                        .up()
                        .add("exception", Exceptions.message(ex))
                        .up()
                )
                .render()
                .status(HttpURLConnection.HTTP_BAD_REQUEST)
                .build();
        }
        throw this.flash().redirect(
            this.uriInfo().getRequestUri(),
            String.format(
                // @checkstyle LineLength (1 line)
                "ACL of stand `%s` successfully updated, will take a few minutes to propagate to all servers",
                this.name
            ),
            Level.INFO
        );
    }

    /**
     * Get Stand.
     * @return Stand
     */
    private Stand stand() {
        final Stand stand;
        try {
            stand = this.users().stand(this.name);
        } catch (NoSuchElementException ex) {
            throw this.flash().redirect(this.uriInfo().getBaseUri(), ex);
        }
        if (!stand.owner().equals(this.user().urn())) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("access denied to stand `%s`", this.name),
                Level.SEVERE
            );
        }
        return stand;
    }

    /**
     * Make a head of the page.
     * @return The page
     */
    private EmptyPage head() {
        return new PageBuilder()
            .stylesheet("/xsl/acl.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(
                new Breadcrumbs()
                    .with("stands")
                    .with("self", this.name)
                    .bundle()
            )
            .link(
                new Link(
                    "save",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(AclRs.class)
                        .path(AclRs.class, "save")
                        .build(this.name)
                )
            );
    }

}
