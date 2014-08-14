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

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.PageBuilder;
import com.rultor.agents.daemons.Home;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * Siblings.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.23
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Path("/p/{name : [/a-zA-Z0-9_\\-\\.]+}")
public final class SiblingsRs extends BaseRs {

    /**
     * Repo name.
     */
    private transient String name;

    /**
     * Inject it from query.
     * @param repo Repo name
     */
    @PathParam("name")
    public void setName(@NotNull(message = "repo name is mandatory")
        final String repo) {
        this.name = repo;
    }

    /**
     * Front page.
     * @param since Date
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index(@QueryParam("since") final String since) {
        final Date date;
        if (since == null || !since.matches("[0-9]+")) {
            date = new Date(Long.MAX_VALUE);
        } else {
            date = new Date(Long.parseLong(since));
        }
        return new PageBuilder()
            .stylesheet("/xsl/siblings.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("repo", this.name))
            .append(new JaxbBundle("since", Long.toString(date.getTime())))
            .append(
                new JaxbBundle("siblings").add(
                    new JaxbBundle.Group<Talk>(
                        Iterables.limit(
                            this.talks().siblings(this.name, date),
                            Tv.TWENTY
                        )
                    ) {
                        @Override
                        public JaxbBundle bundle(final Talk talk) {
                            try {
                                return SiblingsRs.this.bundle(talk);
                            } catch (final IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                    }
                )
            )
            .render()
            .build();
    }

    /**
     * Convert talk to JAXB.
     * @param talk The talk to convert
     * @return JAXB
     * @throws IOException If fails
     */
    private JaxbBundle bundle(final Talk talk) throws IOException {
        if (!this.granted(talk.number())) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "according to .rultor.yml, you're not allowed to see this",
                Level.WARNING
            );
        }
        final XML xml = talk.read();
        final JaxbBundle archive = new JaxbBundle("archive").add(
            new JaxbBundle.Group<XML>(xml.nodes("/talk/archive/log")) {
                @Override
                public JaxbBundle bundle(final XML log) {
                    return SiblingsRs.log(xml, log);
                }
            }
        );
        return new JaxbBundle("talk")
            .add("name", talk.name()).up()
            .add("href", xml.xpath("/talk/wire/href/text()").get(0)).up()
            .add("updated", Long.toString(talk.updated().getTime())).up()
            .add("timeago", new PrettyTime().format(talk.updated())).up()
            .add(archive);
    }

    /**
     * Convert log to JAXB.
     * @param talk Talk
     * @param log The log to convert
     * @return JAXB
     */
    private static JaxbBundle log(final XML talk, final XML log) {
        final String hash = log.xpath("@id").get(0);
        return new JaxbBundle("log")
            .add("id", hash).up()
            .add("href", new Home(talk, hash).uri().toString()).up()
            .add("title", log.xpath("@title").get(0)).up();
    }

}
