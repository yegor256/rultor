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
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Arguments;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulses;
import com.rultor.spi.Repo;
import com.rultor.spi.SpecException;
import com.rultor.spi.Stage;
import com.rultor.spi.Unit;
import com.rultor.spi.Work;
import com.rultor.tools.Markdown;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Drain of a unit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
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
    public void setName(@NotNull(message = "unit name can't be NULL")
        final String unit) {
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
     */
    @GET
    @Path("/")
    public Response index() {
        EmptyPage page = new PageBuilder()
            .stylesheet("/xsl/drain.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("unit", this.name));
        final Drain drain = this.drain(new Time());
        Pulses pulses = this.pulses(drain);
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
                        ExceptionUtils.getRootCauseMessage(ex)
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
        try {
            return Drain.Source.class.cast(
                new Repo.Cached(
                    this.repo(), this.user(), this.unit().spec()
                ).get().instantiate(
                    this.users(),
                    new Arguments(
                        new Work.Simple(
                            this.user().urn(), this.name,
                            this.unit().spec(), time
                        )
                    )
                )
            ).drain();
        } catch (SpecException ex) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "Can't render drain of \"%s\": %s",
                    this.name,
                    ExceptionUtils.getRootCauseMessage(ex)
                ),
                Level.SEVERE
            );
        }
    }

    /**
     * Fetch pulses from drain.
     * @param drain The drain
     * @return The pulses
     */
    private Pulses pulses(final Drain drain) {
        try {
            return drain.pulses();
        } catch (IOException ex) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "I/O problem with the drain of \"%s\": %s",
                    this.name,
                    ExceptionUtils.getRootCauseMessage(ex)
                ),
                Level.SEVERE
            );
        }
    }

    /**
     * Get unit.
     * @return The unit
     */
    private Unit unit() {
        if (!this.user().units().contains(this.name)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("Unit `%s` doesn't exist", this.name),
                Level.SEVERE
            );
        }
        return this.user().get(this.name);
    }

    /**
     * All pulses of the unit.
     * @param pulses All pulses to show
     * @param maximum Maximum to show
     * @return Collection of JAXB units
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
     * @param date Date of it
     * @return Bundle
     */
    private JaxbBundle pulse(final Time date) {
        final PulseOfDrain pulse = new PulseOfDrain(this.drain(date));
        final Collection<Stage> stages;
        try {
            stages = pulse.stages();
        } catch (IOException ex) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "I/O problem with the stages of \"%s\": %s",
                    date,
                    ExceptionUtils.getRootCauseMessage(ex)
                ),
                Level.SEVERE
            );
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
            .add("when", date.when())
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
            .add("output", new Markdown(stage.output()).html())
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
