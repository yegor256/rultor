/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.rexsl.page.JaxbBundle;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Front page of a talk.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/t/{number:[0-9]+}")
public final class TalkRs extends BaseRs {

    /**
     * Talk unique number.
     */
    private transient Long number;

    /**
     * Inject it from query.
     * @param talk Talk name
     */
    @PathParam("number")
    public void setName(@NotNull(message = "talk number is mandatory")
        final Long talk) {
        this.number = talk;
    }

    /**
     * Front page.
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Path("/")
    public Response index() throws IOException {
        this.adminOnly();
        if (!this.talks().exists(this.number)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "there is no such page here",
                Level.WARNING
            );
        }
        final Talk talk = this.talks().get(this.number);
        return new PageBuilder()
            .stylesheet("/xsl/talk.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(
                new JaxbBundle("talk")
                    .add("number", Long.toString(talk.number())).up()
                    .add("name", talk.name()).up()
                    .add("content", talk.read().toString()).up()
            )
            .render()
            .build();
    }

}
