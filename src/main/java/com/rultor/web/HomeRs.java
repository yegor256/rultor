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
import com.google.common.collect.Lists;
import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Pulse;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.ocpsoft.prettytime.PrettyTime;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Path("/")
public final class HomeRs extends BaseRs {

    /**
     * XSLT for pulse render.
     */
    private static final XSL PULSE = XSLDocument.make(
        HomeRs.class.getResourceAsStream("pulse.xsl")
    );

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/home.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(
                new JaxbBundle("recent").add(
                    new JaxbBundle.Group<Talk>(
                        Iterables.limit(this.talks().recent(), Tv.FIVE)
                    ) {
                        @Override
                        public JaxbBundle bundle(final Talk talk) {
                            try {
                                return HomeRs.bundle(talk);
                            } catch (final IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                    }
                )
            )
            .link(new Link("svg", "./svg"))
            .render()
            .build();
    }

    /**
     * Old stands.
     * @param name Name
     * @return The JAX-RS response
     */
    @GET
    @Path("/s/{name}")
    public Response stand(@PathParam("name") final String name) {
        throw this.flash().redirect(
            this.uriInfo().getBaseUri(),
            String.format("stand %s is not available any more", name),
            Level.WARNING
        );
    }

    /**
     * Build SVG.
     * @return The JAX-RS response
     * @since 1.21
     * @throws ImpossibleModificationException If fails
     */
    @GET
    @Path("/svg")
    @Produces("image/svg+xml")
    public String svg() throws ImpossibleModificationException {
        return HomeRs.PULSE.transform(
            HomeRs.dirs(this.ticks())
        ).nodes("/*").get(0).toString();
    }

    /**
     * Turn talk into a JAXB bundle.
     * @param talk Talk
     * @return Bundle
     * @throws IOException If fails
     */
    private static JaxbBundle bundle(final Talk talk) throws IOException {
        JaxbBundle bundle = new JaxbBundle("talk", talk.name())
            .attr("timeago", new PrettyTime().format(talk.updated()));
        final XML xml = talk.read();
        if (!xml.nodes("/talk/wire/href").isEmpty()) {
            bundle = bundle.attr(
                "href",
                talk.read().xpath("/talk/wire/href/text()").get(0)
            );
        }
        return bundle;
    }

    /**
     * Turn ticks into XML.
     * @param ticks Ticks
     * @return XML
     * @throws ImpossibleModificationException If fails
     */
    private static XML dirs(final Collection<Pulse.Tick> ticks)
        throws ImpossibleModificationException {
        final long now = System.currentTimeMillis();
        final Directives dirs = new Directives().add("pulse");
        for (final Pulse.Tick tick : ticks) {
            dirs.add("tick")
                .attr("total", Integer.toString(tick.total()))
                .attr("start", Long.toString(tick.start() - now))
                .attr("msec", Long.toString(tick.duration()))
                .up();
        }
        return new XMLDocument(new Xembler(dirs).xml());
    }

    /**
     * Get all ticks.
     * @return Ticks
     */
    private Collection<Pulse.Tick> ticks() {
        return Lists.newArrayList(
            Pulse.class.cast(
                this.servletContext().getAttribute(Pulse.class.getName())
            ).ticks()
        );
    }

}
