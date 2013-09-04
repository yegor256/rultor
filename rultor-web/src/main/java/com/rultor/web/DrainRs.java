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
import com.jcabi.log.Logger;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XSLT;
import com.rultor.spi.Arguments;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Repo;
import com.rultor.spi.Rule;
import com.rultor.spi.SpecException;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import org.xembly.ImpossibleModificationException;
import org.xembly.XemblySyntaxException;

/**
 * Drain of a rule.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/drain/{rule:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.ExcessiveImports")
public final class DrainRs extends BaseRs {

    /**
     * Query param.
     */
    private static final String QUERY_SINCE = "since";

    /**
     * Rule name.
     */
    private transient String name;

    /**
     * Since (date).
     */
    private transient Time since;

    /**
     * Inject it from query.
     * @param rule Rule name
     */
    @PathParam("rule")
    public void setName(@NotNull(message = "rule name can't be NULL")
        final String rule) {
        this.name = rule;
    }

    /**
     * Inject it from query.
     * @param time Since what time
     */
    @QueryParam(DrainRs.QUERY_SINCE)
    public void setSince(final String time) {
        if (time != null) {
            this.since = new Time(Long.parseLong(time));
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
            .stylesheet("/xsl/drain.xsl")
            .build(EmptyPage.class)
            .init(this)
            .link(
                new Link(
                    "edit",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(RuleRs.class)
                        .build(this.name)
                )
            )
            .append(
                new Breadcrumbs()
                    .with("rules")
                    .with("edit", this.name)
                    .with("self", "drain")
                    .bundle()
            )
            .append(new JaxbBundle("rule", this.name));
        final Drain drain = this.drain(new Time());
        Pageable<Time, Time> pulses = this.pulses(drain);
        final int total;
        if (this.since == null) {
            total = Tv.FIVE;
        } else {
            try {
                pulses = pulses.tail(this.since);
            } catch (IOException ex) {
                throw this.flash().redirect(
                    this.uriInfo().getBaseUri(),
                    String.format(
                        "I/O problem with the tail of drain of \"%s\": %s",
                        this.name,
                        Exceptions.message(ex)
                    ),
                    Level.SEVERE
                );
            }
            total = Tv.TWENTY;
            page = page
                .append(new JaxbBundle("since", this.since.toString()))
                .link(
                    new Link(
                        "latest",
                        this.uriInfo().getBaseUriBuilder()
                            .clone()
                            .path(DrainRs.class)
                            .build(this.name)
                    )
                );
        }
        return page
            .append(this.pulses(pulses.iterator(), total))
            .render()
            .build();
    }

    /**
     * Get drain.
     * @param time Time where we should create a drain
     * @return The drain
     */
    private Drain drain(final Time time) {
        final Object src;
        try {
            src = new Repo.Cached(
                this.repo(), this.user(), this.rule().spec()
            ).get().instantiate(
                this.users(),
                new Arguments(
                    new Work.Simple(this.user().urn(), this.name, time),
                    new Wallet.Empty()
                )
            );
        } catch (SpecException ex) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "Can't render drain of \"%s\": %s",
                    this.name,
                    Exceptions.message(ex)
                ),
                Level.SEVERE
            );
        }
        if (!(src instanceof Drain.Source)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                Logger.format(
                    "Rule `%[type]s` is not an instance of `Drain.Source`", src
                ),
                Level.SEVERE
            );
        }
        return Drain.Source.class.cast(src).drain();
    }

    /**
     * Fetch pulses from drain.
     * @param drain The drain
     * @return The pulses
     */
    private Pageable<Time, Time> pulses(final Drain drain) {
        try {
            return drain.pulses();
        } catch (IOException ex) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "I/O problem with the drain of \"%s\": %s",
                    this.name,
                    Exceptions.message(ex)
                ),
                Level.SEVERE
            );
        }
    }

    /**
     * Get rule.
     * @return The rule
     */
    private Rule rule() {
        try {
            return this.user().rules().get(this.name);
        } catch (NoSuchElementException ex) {
            throw this.flash().redirect(this.uriInfo().getBaseUri(), ex);
        }
    }

    /**
     * All pulses of the rule.
     * @param pulses All pulses to show
     * @param maximum Maximum to show
     * @return Collection of JAXB rules
     */
    private JaxbBundle pulses(final Iterator<Time> pulses, final int maximum) {
        JaxbBundle bundle = new JaxbBundle("pulses");
        int pos;
        for (pos = 0; pos < maximum; ++pos) {
            if (!pulses.hasNext()) {
                break;
            }
            bundle = bundle.add(this.pulse(pulses.next()));
        }
        if (pos == maximum && pulses.hasNext()) {
            bundle = bundle.link(
                new Link(
                    "more",
                    this.uriInfo()
                        .getBaseUriBuilder()
                        .clone()
                        .path(DrainRs.class)
                        .queryParam(
                            DrainRs.QUERY_SINCE,
                            pulses.next().millis()
                        )
                        .build(this.name)
                )
            );
        }
        return bundle;
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param time Date of it
     * @return Bundle
     */
    private JaxbBundle pulse(final Time time) {
        final Collection<Exception> bugs = new LinkedList<Exception>();
        JaxbBundle bundle = new JaxbBundle("pulse")
            .add("time", time.toString())
            .up()
            .link(
                new Link(
                    // @checkstyle MultipleStringLiterals (1 line)
                    "stream",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(PulseRs.class)
                        .path(PulseRs.class, "stream")
                        .build(this.name, time.millis())
                )
            );
        final Snapshot snapshot;
        try {
            snapshot = new Snapshot(this.drain(time).read());
            bundle = bundle.add("xembly", snapshot.xembly()).up();
            try {
                bundle = bundle.add(
                    new XSLT(
                        snapshot,
                        this.getClass().getResourceAsStream("post.xsl")
                    ).dom().getDocumentElement()
                );
            } catch (ImpossibleModificationException ex) {
                bugs.add(ex);
            }
        } catch (IOException ex) {
            bugs.add(ex);
        } catch (XemblySyntaxException ex) {
            bugs.add(ex);
        } catch (TransformerException ex) {
            bugs.add(ex);
        }
        return bundle.add(
            new JaxbBundle("exceptions").add(
                new JaxbBundle.Group<Exception>(bugs) {
                    @Override
                    public JaxbBundle bundle(final Exception bug) {
                        return new JaxbBundle("exception")
                            .add("class", bug.getClass().getCanonicalName())
                            .up()
                            .add(
                                "message",
                                Exceptions.message(bug)
                            )
                            .up();
                    }
                }
            )
        );
    }

}
