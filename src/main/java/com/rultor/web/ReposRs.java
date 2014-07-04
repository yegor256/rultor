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

import com.jcabi.github.Coordinates;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import com.rultor.spi.Repo;
import java.io.IOException;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/r")
public final class ReposRs extends BaseRs {

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/repos.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new Breadcrumbs().with("self", "home").bundle())
            .append(
                new JaxbBundle("repos").add(
                    new JaxbBundle.Group<Repo>(this.user().repos().iterate()) {
                        @Override
                        public JaxbBundle bundle(final Repo repo) {
                            try {
                                return ReposRs.this.bundle(repo);
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
     * Add one repo.
     * @param coords Coordinates
     * @throws IOException If fails
     */
    @GET
    @Path("/add")
    public void add(@QueryParam("coords") final String coords)
        throws IOException {
        final long num = this.user().repos().add(new Coordinates.Simple(coords));
        throw FlashInset.forward(
            this.uriInfo().getBaseUriBuilder()
                .clone()
                .path(ReposRs.class)
                .build(),
            String.format("repository '%s' added as #%d", coords, num),
            Level.INFO
        );
    }

    /**
     * Delete one repo.
     * @param number Its number
     */
    @GET
    @Path("/delete")
    public void delete(@QueryParam("num") final Long number) {
        this.user().repos().delete(number);
        throw FlashInset.forward(
            this.uriInfo().getBaseUriBuilder()
                .clone()
                .path(ReposRs.class)
                .build(),
            String.format("repository #%d was deleted", number),
            Level.INFO
        );
    }

    /**
     * Convert repo to JaxbBundle.
     * @param repo The repo
     * @return Bundle
     * @throws IOException If fails
     */
    private JaxbBundle bundle(final Repo repo) throws IOException {
        return new JaxbBundle("repo")
            .add("number", Long.toString(repo.number()))
            .up()
            .add("coordinates", repo.coordinates().toString())
            .up()
            .link(
                new Link(
                    "open",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(RepoRs.class)
                        .build(repo.number())
                )
            )
            .up()
            .link(
                new Link(
                    "delete",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(ReposRs.class)
                        .path(ReposRs.class, "delete")
                        .queryParam("n", "{n1}")
                        .build(repo.number())
                )
            )
            .up();
    }

}
