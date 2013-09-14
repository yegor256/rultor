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
import com.rultor.spi.Stand;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * List of user's stands.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/stands")
@Loggable(Loggable.DEBUG)
public final class StandsRs extends BaseRs {

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/stands.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new Breadcrumbs().with("self", "stands").bundle())
            .append(this.mine())
            .link(new Link("create", "./create"))
            .render()
            .build();
    }

    /**
     * Create new empty stand.
     * @param name Name of the stand to create
     * @return The JAX-RS response
     */
    @POST
    @Path("/create")
    public Response create(@NotNull(message = "stand name is mandatory")
        @FormParam("name") final String name) {
        if (this.user().stands().contains(name)) {
            throw this.flash().redirect(
                this.uriInfo().getRequestUri(),
                String.format("Stand `%s` already exists", name),
                Level.WARNING
            );
        }
        this.user().stands().create(name);
        throw this.flash().redirect(
            this.uriInfo().getBaseUriBuilder()
                .clone()
                .path(AclRs.class)
                .build(name),
            String.format("Stand `%s` successfully created", name),
            Level.INFO
        );
    }

    /**
     * All my stands.
     * @return Collection of JAXB stands
     */
    private JaxbBundle mine() {
        return new JaxbBundle("stands").add(
            new JaxbBundle.Group<Stand>(this.user().stands()) {
                @Override
                public JaxbBundle bundle(final Stand stand) {
                    return StandsRs.this.stand(stand);
                }
            }
        );
    }

    /**
     * Convert stand to JaxbBundle.
     * @param stand Name of stand
     * @return Bundle
     */
    private JaxbBundle stand(final Stand stand) {
        return new JaxbBundle("stand")
            .add("name", stand.name())
            .up()
            .link(
                new Link(
                    "acl",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(AclRs.class)
                        .build(stand.name())
                )
            )
            .link(
                new Link(
                    "see",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(StandRs.class)
                        .build(stand.name())
                )
            );
    }

}
