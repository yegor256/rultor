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
import com.rultor.spi.Pulse;
import com.rultor.spi.Stage;
import com.rultor.spi.Unit;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Pulses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/pulses/{unit:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
public final class PulsesRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_SINCE = "since";

    /**
     * Maximum number of pulses to show.
     */
    private static final int MAX = 20;

    /**
     * Unit name.
     */
    private transient String name;

    /**
     * Since (date).
     */
    private transient Date since;

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
    @QueryParam(PulsesRs.QUERY_SINCE)
    public void setSince(final String time) {
        if (time == null) {
            this.since = new Date(Long.MAX_VALUE);
        } else {
            this.since = new Date(Long.parseLong(time));
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
        return new PageBuilder()
            .stylesheet("/xsl/pulses.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("unit", this.name))
            .append(this.pulses())
            .render()
            .build();
    }

    /**
     * Get unit.
     * @return The unit
     */
    private Unit unit() {
        final Unit unit = this.user().units().get(this.name);
        if (unit == null) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("Unit '%s' doesn't exist", this.name),
                Level.SEVERE
            );
        }
        return unit;
    }

    /**
     * All pulses of the unit.
     * @return Collection of JAXB units
     */
    private JaxbBundle pulses() {
        final SortedMap<Date, Pulse> pulses =
            this.unit().pulses().tailMap(this.since);
        return new JaxbBundle("pulses").add(
            new JaxbBundle.Group<Map.Entry<Date, Pulse>>(pulses.entrySet()) {
                @Override
                public JaxbBundle bundle(final Map.Entry<Date, Pulse> ent) {
                    return PulsesRs.this.pulse(ent.getKey(), ent.getValue());
                }
            }
        );
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param date Date of it
     * @param pulse Pulse to convert
     * @return Bundle
     */
    private JaxbBundle pulse(final Date date, final Pulse pulse) {
        return new JaxbBundle("pulse")
            .add("stages")
            .add(
                new JaxbBundle.Group<Stage>(pulse.stages()) {
                    @Override
                    public JaxbBundle bundle(final Stage stage) {
                        return PulsesRs.this.stage(date, stage);
                    }
                }
            )
            .up()
            .add(
                "date",
                DateFormatUtils.formatUTC(date, "yyyy-MM-dd'T'HH:mm'Z'")
            )
            .up()
            .link(
                new Link(
                    "see",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(PulseRs.class)
                        .build(this.name, date.getTime())
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
                        .build(this.name, date.getTime())
                )
            );
    }

    /**
     * Convert stage to JaxbBundle.
     * @param date Date of it
     * @param stage Stage to convert
     * @return Bundle
     */
    private JaxbBundle stage(final Date date, final Stage stage) {
        return new JaxbBundle("stage")
            .add("result", stage.result().toString())
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
                        .build(this.name, date.getTime())
                )
            );
    }

}
