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
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Repo;
import com.rultor.spi.Talk;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Repo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/repo/{num:[0-9]+}")
public final class RepoRs extends BaseRs {

    /**
     * Repo's number.
     */
    private transient Long number;

    /**
     * Inject it from query.
     * @param num Number
     */
    @PathParam("num")
    public void setNum(@NotNull(message = "number is mandatory")
        final Long num) {
        this.number = num;
    }

    /**
     * View an existing rule or an empty one.
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Path("/")
    public Response index() throws IOException {
        final Repo repo = this.user().repos().get(this.number);
        return new PageBuilder()
            .stylesheet("/xsl/repos.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new Breadcrumbs().with("self", "home").bundle())
            .append(
                new JaxbBundle("talks").add(
                    new JaxbBundle.Group<Talk>(repo.talks().iterate()) {
                        @Override
                        public JaxbBundle bundle(final Talk talk) {
                            try {
                                return RepoRs.this.bundle(talk);
                            } catch (final IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                    }

                )
            )
            .link(new Link("add", "./add"))
            .render()
            .build();
    }

    /**
     * Convert talk to JaxbBundle.
     * @param talk The talk
     * @return Bundle
     * @throws IOException If fails
     */
    private JaxbBundle bundle(final Talk talk) throws IOException {
        return new JaxbBundle("talk")
            .add("name", talk.name())
            .up()
            .add("content", talk.read().toString())
            .up();
    }

}
