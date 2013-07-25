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
import com.rultor.timeline.Timeline;
import com.rultor.timeline.Timelines;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Edit timeline.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/e/{name:[a-z]+}")
@Loggable(Loggable.DEBUG)
public final class EditRs extends BaseRs {

    /**
     * Timeline.
     */
    private transient Timeline timeline;

    /**
     * Inject it from query.
     * @param name Name of get
     */
    @PathParam("name")
    public void setName(@NotNull(message = "unit name can't be NULL")
        final String name) {
        try {
            this.timeline = this.timelines().get(name);
        } catch (Timelines.TimelineNotFoundException ex) {
            throw new WebApplicationException(
                ex, HttpURLConnection.HTTP_NOT_FOUND
            );
        }
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/edit.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(
                new JaxbBundle("timeline")
                    .add("name", this.timeline.name())
                    .up()
                    .add("key", this.timeline.permissions().key())
                    .up()
                    .add("friends")
                    .add(
                        new JaxbBundle.Group<String>(
                            this.timeline.permissions().friends()) {
                            @Override
                            public JaxbBundle bundle(final String friend) {
                                return new JaxbBundle("friend", friend);
                            }
                        }
                    )
                    .up()
            )
            .link(
                new Link(
                    // @checkstyle MultipleStringLiterals (1 line)
                    "save",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(EditRs.class)
                        .path(EditRs.class, "save")
                        .build(this.timeline.name())
                )
            )
            .render()
            .build();
    }

    /**
     * Save it.
     * @param key Key to set
     * @param friends List of friends
     * @return The JAX-RS response
     */
    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response save(@FormParam("key") @NotNull final String key,
        @FormParam("friends") @NotNull final String friends) {
        this.timeline.permissions().key(key);
        this.timeline.permissions().friends(
            Arrays.asList(friends.split("\\s+"))
        );
        throw this.flash().redirect(
            this.uriInfo().getBaseUri(),
            String.format(
                "Timeline `%s` updated successfully",
                this.timeline.name()
            ),
            Level.INFO
        );
    }

}
