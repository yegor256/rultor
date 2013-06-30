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

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulse;
import com.rultor.spi.Pulses;
import com.rultor.spi.Repo;
import com.rultor.spi.Stage;
import com.rultor.spi.Time;
import com.rultor.spi.Unit;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Drain of a unit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/drain/{unit:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
public final class DrainRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_SINCE = "since";

    /**
     * Unit name.
     */
    private transient String name;

    /**
     * Since (date).
     */
    private transient Time since;

    /**
     * Inject it from query.
     * @param unit Unit name
     */
    @PathParam("unit")
    public void setName(@NotNull final String unit) {
        this.name = unit;
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
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/")
    public Response index() throws Exception {
        EmptyPage page = new PageBuilder()
            .stylesheet("/xsl/drain.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("unit", this.name));
        final Drain drain = Drain.class.cast(
            new Repo.Cached(this.repo(), this.user(), this.unit().drain()).get()
        );
        Pulses pulses = drain.pulses();
        final Iterable<Time> visible;
        if (this.since == null) {
            visible = Iterables.limit(pulses, Tv.TEN);
        } else {
            pulses = pulses.tail(this.since);
            visible = Iterables.limit(pulses, Tv.TWENTY);
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
        final int total = Iterables.size(visible);
        if (Iterables.size(Iterables.limit(pulses, total + 1)) > total) {
            page = page.link(
                new Link(
                    "more",
                    this.uriInfo()
                        .getBaseUriBuilder()
                        .clone()
                        .path(DrainRs.class)
                        .queryParam(
                            DrainRs.QUERY_SINCE,
                            Iterables.getLast(visible).millis()
                        )
                        .build(this.name)
                )
            );
        }
        return page.append(this.pulses(drain, visible)).render().build();
    }

    /**
     * Get unit.
     * @return The unit
     */
    private Unit unit() {
        if (!this.user().units().contains(this.name)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("Unit '%s' doesn't exist", this.name),
                Level.SEVERE
            );
        }
        return this.user().get(this.name);
    }

    /**
     * All pulses of the unit.
     * @param drain Drain to get data from
     * @param pulses All pulses to show
     * @return Collection of JAXB units
     * @throws Exception If fails
     */
    private JaxbBundle pulses(final Drain drain,
        final Iterable<Time> pulses) throws Exception {
        return new JaxbBundle("pulses").add(
            new JaxbBundle.Group<Time>(pulses) {
                @Override
                public JaxbBundle bundle(final Time date) {
                    return DrainRs.this.pulse(drain, date);
                }
            }
        );
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param drain Drain we're in
     * @param date Date of it
     * @return Bundle
     */
    private JaxbBundle pulse(final Drain drain, final Time date) {
        final Pulse pulse = new Pulse(date, drain);
        final Collection<Stage> stages;
        try {
            stages = pulse.stages();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return new JaxbBundle("pulse")
            .add("stages")
            .add(
                new JaxbBundle.Group<Stage>(stages) {
                    @Override
                    public JaxbBundle bundle(final Stage stage) {
                        return DrainRs.this.stage(date, stage);
                    }
                }
            )
            .up()
            .add("date", date.toString())
            .up()
            .link(
                new Link(
                    "see",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(PulseRs.class)
                        .build(this.name, date.millis())
                )
            )
            .link(
                new Link(
                    // @checkstyle MultipleStringLiterals (1 line)
                    "stream",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(PulseRs.class)
                        .path(PulseRs.class, "stream")
                        .build(this.name, date.millis())
                )
            );
    }

    /**
     * Convert stage to JaxbBundle.
     * @param date Date of it
     * @param stage Stage to convert
     * @return Bundle
     */
    private JaxbBundle stage(final Time date, final Stage stage) {
        return new JaxbBundle("stage")
            .add("result", stage.result().toString())
            .up()
            .add("start", Long.toString(stage.start()))
            .up()
            .add("stop", Long.toString(stage.stop()))
            .up()
            .add("msec", Long.toString(stage.stop() - stage.start()))
            .up()
            .add("output", stage.output())
            .up()
            .link(
                new Link(
                    "log",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(PulseRs.class)
                        .queryParam(PulseRs.QUERY_START, stage.start())
                        .queryParam(PulseRs.QUERY_STOP, stage.stop())
                        .build(this.name, date.millis())
                )
            );
    }

}
