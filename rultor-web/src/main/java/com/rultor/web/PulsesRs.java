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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
@Path("/ps")
@Loggable(Loggable.DEBUG)
public final class PulsesRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_NAME = "name";

    /**
     * Query param.
     */
    public static final String QUERY_PAGE = "p";

    /**
     * Page size.
     */
    private static final int PAGE_SIZE = 20;

    /**
     * Unit name.
     */
    private transient String name;

    /**
     * Page number.
     */
    private transient int page;

    /**
     * Inject it from query.
     * @param unit Unit name
     */
    @QueryParam(PulsesRs.QUERY_NAME)
    public void setName(@NotNull final String unit) {
        this.name = unit;
    }

    /**
     * Inject it from query.
     * @param num Page number
     */
    @QueryParam(PulsesRs.QUERY_PAGE)
    public void setPage(final String num) {
        if (num == null) {
            this.page = 0;
        } else {
            this.page = Integer.parseInt(num);
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
        return this.user().units().get(this.name);
    }

    /**
     * All pulses of the unit.
     * @return Collection of JAXB units
     */
    private JaxbBundle pulses() {
        final List<Pulse> pulses = this.unit().pulses();
        final int from = this.page * PulsesRs.PAGE_SIZE;
        if (from >= pulses.size()) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "Page #%d is out of boundary",
                    this.page
                ),
                Level.SEVERE
            );
        }
        int till = (this.page + 1) * PulsesRs.PAGE_SIZE;
        if (till >= pulses.size()) {
            till = pulses.size();
        }
        final AtomicInteger pos = new AtomicInteger(from);
        return new JaxbBundle("pulses").add(
            new JaxbBundle.Group<Pulse>(pulses.subList(from, till)) {
                @Override
                public JaxbBundle bundle(final Pulse pulse) {
                    return PulsesRs.this.pulse(pos.getAndIncrement(), pulse);
                }
            }
        );
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param pos Position
     * @param pulse Pulse to convert
     * @return Bundle
     */
    private JaxbBundle pulse(final int pos, final Pulse pulse) {
        return new JaxbBundle("pulse")
            .add("spec", pulse.spec().asText())
            .up()
            .add(
                "started",
                DateFormatUtils.ISO_DATETIME_FORMAT.format(pulse.started())
            )
            .up()
            .add("stages")
            .add(
                new JaxbBundle.Group<Stage>(pulse.stages()) {
                    @Override
                    public JaxbBundle bundle(final Stage stage) {
                        return PulsesRs.this.stage(pulse, stage);
                    }
                }
            )
            .up()
            .link(
                new Link(
                    "see",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(PulseRs.class)
                        .queryParam(PulseRs.QUERY_NAME, "{n}")
                        .queryParam(PulseRs.QUERY_DATE, "{d}")
                        .queryParam(PulseRs.QUERY_POSITION, "{p}")
                        .build(this.name, pulse.started().getTime(), pos)
                )
            );
    }

    /**
     * Convert stage to JaxbBundle.
     * @param pulse Pulse
     * @param stage Stage to convert
     * @return Bundle
     */
    private JaxbBundle stage(final Pulse pulse, final Stage stage) {
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
                        .queryParam(PulseRs.QUERY_NAME, "{x}")
                        .queryParam(PulseRs.QUERY_DATE, "{y}")
                        .queryParam(PulseRs.QUERY_START, stage.start())
                        .queryParam(PulseRs.QUERY_STOP, stage.stop())
                        .build(this.name, pulse.started().getTime())
                )
            );
    }

}
