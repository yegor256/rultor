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
import com.jcabi.aspects.Tv;
import com.jcabi.immutable.ArraySet;
import com.jcabi.urn.URN;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.auth.Identity;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XSLT;
import com.rultor.spi.ACL;
import com.rultor.spi.Arguments;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulse;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.SpecException;
import com.rultor.spi.Stand;
import com.rultor.spi.Tag;
import com.rultor.spi.User;
import com.rultor.spi.Wallet;
import com.rultor.spi.Widget;
import com.rultor.tools.Exceptions;
import com.rultor.widget.Alert;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;
import org.xembly.XemblySyntaxException;

/**
 * Stand front page.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle ClassFanOutComplexity (500 lines)
 */
@Path("/s/{stand:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.TooManyMethods" })
public final class StandRs extends BaseRs {

    /**
     * List of open pulses.
     */
    private static final String QUERY_OPEN = "open";

    /**
     * ID of pulse.
     */
    private static final String QUERY_ID = "id";

    /**
     * Stand name.
     */
    private transient String name;

    /**
     * Names of pulses to show open.
     */
    private final transient Set<Coordinates> open = new TreeSet<Coordinates>();

    /**
     * Inject it from query.
     * @param stand Stand name
     */
    @PathParam("stand")
    public void setName(@NotNull(message = "stand name can't be NULL")
        final String stand) {
        this.name = stand;
    }

    /**
     * Inject it from query.
     * @param pulses Names of pulses
     */
    @QueryParam(StandRs.QUERY_OPEN)
    public void setPulses(final List<String> pulses) {
        if (pulses != null) {
            for (String pulse : pulses) {
                this.open.add(Coordinates.Simple.valueOf(pulse));
            }
        }
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        EmptyPage page = new PageBuilder()
            .stylesheet("/xsl/stand.xsl")
            .build(EmptyPage.class)
            .init(this)
            .link(
                new Link(
                    "edit",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(AclRs.class)
                        .build(this.name)
                )
            )
            .link(
                new Link(
                    "collapse",
                    this.self(new ArrayList<Coordinates>(0))
                )
            );
        if (this.auth().identity().equals(Identity.ANONYMOUS)) {
            page = page.append(new Breadcrumbs().with("home").bundle());
        } else {
            page = page.append(
                new Breadcrumbs()
                    .with("stands")
                    .with("edit", this.name)
                    .with("collapse", "stand")
                    .bundle()
            );
        }
        return page
            .append(new JaxbBundle("stand", this.name))
            .append(this.widgets(this.widgets(this.stand().widgets())))
            .append(this.pulses(this.stand().pulses().iterator(), Tv.TWENTY))
            .render()
            .build();
    }

    /**
     * Get snapshot HTML for a pulse.
     * @param uid Unique identifier of a pulse
     * @return The JAX-RS response
     */
    @GET
    @Path("/fetch")
    @Produces(MediaType.TEXT_HTML)
    public Response fetch(@QueryParam(StandRs.QUERY_ID) final String uid) {
        final Coordinates coords = Coordinates.Simple.valueOf(uid);
        Response resp;
        try {
            resp = Response.ok().entity(
                new XSLT(
                    this.render(
                        new JaxbBundle("div"),
                        this.stand().pulses().tail(coords).iterator().next()
                    ).element(),
                    this.getClass().getResourceAsStream("fetch.xsl")
                ).xml()
            ).build();
        } catch (TransformerException ex) {
            resp = Response.serverError().entity(
                Exceptions.stacktrace(ex)
            ).build();
        } catch (IOException ex) {
            resp = Response.serverError().entity(
                Exceptions.stacktrace(ex)
            ).build();
        }
        return resp;
    }

    /**
     * Get stand.
     * @return The stand
     */
    private Stand stand() {
        final Stand stand;
        try {
            stand = this.users().stand(this.name);
        } catch (NoSuchElementException ex) {
            throw this.flash().redirect(this.uriInfo().getBaseUri(), ex);
        }
        if (!stand.owner().equals(this.auth().identity().urn())
            && !this.acl(stand).canView(this.auth().identity().urn())) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("access denied to stand `%s`", this.name),
                Level.SEVERE
            );
        }
        return stand;
    }

    /**
     * All pulses of the stand.
     * @param pulses All pulses to show
     * @param maximum Maximum to show
     * @return Collection of JAXB stands
     */
    private JaxbBundle pulses(final Iterator<Pulse> pulses, final int maximum) {
        JaxbBundle bundle = new JaxbBundle("pulses");
        int pos;
        for (pos = 0; pos < maximum; ++pos) {
            if (!pulses.hasNext()) {
                break;
            }
            bundle = bundle.add(this.pulse(pulses.next()));
        }
        return bundle;
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param pulse The pulse
     * @return Bundle
     */
    private JaxbBundle pulse(final Pulse pulse) {
        final Coordinates coords = pulse.coordinates();
        JaxbBundle bundle = new JaxbBundle("pulse")
            .add("coordinates")
            .add("rule", coords.rule()).up()
            .add("owner", coords.owner().toString()).up()
            .add("scheduled", coords.scheduled().toString()).up()
            .up();
        final ArraySet<Coordinates> now = new ArraySet<Coordinates>(this.open);
        if (this.open.contains(coords)) {
            bundle = this
                .render(bundle, pulse)
                .link(new Link("close", this.self(now.without(coords))))
                .link(
                    new Link(
                        "fetch",
                        this.uriInfo().getBaseUriBuilder()
                            .clone()
                            .path(StandRs.class)
                            .path(StandRs.class, "fetch")
                            .queryParam(StandRs.QUERY_ID, "{id}")
                            .build(this.name, coords)
                    )
                );
        } else {
            bundle = bundle
                .link(new Link("open", this.self(now.with(coords))))
                .add("tags")
                .add(
                    new JaxbBundle.Group<Tag>(pulse.tags()) {
                        @Override
                        public JaxbBundle bundle(final Tag tag) {
                            return new JaxbBundle("tag")
                                .add("label", tag.label()).up()
                                .add("level", tag.level().toString()).up();
                        }
                    }
                )
                .up();
        }
        return bundle;
    }

    /**
     * Build URI to itself with new list of open pulses.
     * @param coords Names of pulses
     * @return URI
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private URI self(final Collection<Coordinates> coords) {
        final UriBuilder builder = this.uriInfo().getBaseUriBuilder()
            .clone().path(StandRs.class);
        final Object[] args = new Object[coords.size() + 1];
        final Object[] labels = new String[coords.size()];
        args[0] = this.name;
        int idx = 0;
        for (Coordinates coord : coords) {
            labels[idx] = String.format("{arg%d}", idx);
            args[idx + 1] = new Coordinates.Simple(coord).toString();
            ++idx;
        }
        return builder.queryParam(StandRs.QUERY_OPEN, labels).build(args);
    }

    /**
     * Render snapshot into bundle.
     * @param bundle Bundle to render into
     * @param pulse The pulse
     * @return Bundle
     */
    private JaxbBundle render(final JaxbBundle bundle, final Pulse pulse) {
        JaxbBundle output = bundle;
        try {
            final Snapshot snapshot = this.snapshot(pulse.xembly());
            try {
                output = output.add(
                    new XSLT(
                        snapshot,
                        this.getClass().getResourceAsStream("post.xsl")
                    ).dom().getDocumentElement()
                );
            } catch (ImpossibleModificationException ex) {
                output = this.bug(output, ex);
            } catch (TransformerException ex) {
                output = this.bug(output, ex);
            }
        } catch (IOException ex) {
            output = this.bug(output, ex);
        } catch (XemblySyntaxException ex) {
            output = this.bug(output, ex);
        }
        return output;
    }

    /**
     * Get snapshot from xembly.
     * @param xembly Xembly script
     * @return Its snapshot
     * @throws XemblySyntaxException If fails
     * @checkstyle RedundantThrows (5 lines)
     */
    private Snapshot snapshot(final String xembly)
        throws XemblySyntaxException {
        return new Snapshot(
            new Directives(xembly).xpath("/snapshot/spec").remove()
        );
    }

    /**
     * Add bug to bundle.
     * @param bundle Bundle to render into
     * @param exc Exception
     * @return Bundle
     */
    private JaxbBundle bug(final JaxbBundle bundle, final Exception exc) {
        return bundle.add("error", Exceptions.message(exc)).up();
    }

    /**
     * Get ACL of the stand.
     * @param stand The stand
     * @return ACL
     */
    private ACL acl(final Stand stand) {
        ACL acl;
        try {
            acl = ACL.class.cast(
                new Repo.Cached(this.repo(), new User.Nobody(), stand.acl())
                    .get()
                    .instantiate(
                        this.users(),
                        new Arguments(
                            new Coordinates.None(), new Wallet.Empty()
                        )
                    )
            );
        } catch (SpecException ex) {
            Exceptions.warn(this, ex);
            acl = new ACL() {
                @Override
                public boolean canView(final URN urn) {
                    return false;
                }
                @Override
                public boolean canPost(final String key) {
                    return false;
                }
            };
        }
        return acl;
    }

    /**
     * All widgets of the stand.
     * @param spec Spec of array of widgets
     * @return Collection of JAXB widgets
     */
    @SuppressWarnings("unchecked")
    private Collection<Widget> widgets(final Spec spec) {
        Collection<Widget> list;
        try {
            list = Collection.class.cast(
                new Repo.Cached(this.repo(), new User.Nobody(), spec)
                    .get()
                    .instantiate(
                        this.users(),
                        new Arguments(
                            new Coordinates.None(), new Wallet.Empty()
                        )
                    )
            );
        } catch (SpecException ex) {
            list = Arrays.<Widget>asList(new Alert(Exceptions.stacktrace(ex)));
        }
        return list;
    }

    /**
     * All widgets of the stand.
     * @param widgets Collection of widgets
     * @return Collection of JAXB widgets
     */
    private JaxbBundle widgets(final Collection<Widget> widgets) {
        JaxbBundle bundle = new JaxbBundle("widgets");
        for (Widget widget : widgets) {
            bundle = bundle.add(this.widget(widget));
        }
        return bundle;
    }

    /**
     * Render one widget.
     * @param widget Widget to render
     * @return DOM element
     */
    private Element widget(final Widget widget) {
        final Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        dom.appendChild(dom.createElement("widget"));
        try {
            new Xembler(widget.render(this.stand())).apply(dom);
        } catch (ImpossibleModificationException ex) {
            final Element error = dom.createElement("error");
            error.setTextContent(Exceptions.stacktrace(ex));
            dom.getDocumentElement().appendChild(error);
        }
        return dom.getDocumentElement();
    }

}
