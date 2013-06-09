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
import com.rultor.users.Pulse;
import com.rultor.users.Stage;
import com.rultor.users.Unit;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

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
     * Unit name.
     */
    private transient String name;

    /**
     * Inject it from query.
     * @param unit Unit name
     */
    @QueryParam(PulsesRs.QUERY_NAME)
    public void setName(@NotNull final String unit) {
        this.name = unit;
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
        return new JaxbBundle("pulses").add(
            new JaxbBundle.Group<Pulse>(this.unit().pulses()) {
                @Override
                public JaxbBundle bundle(final Pulse pulse) {
                    return PulsesRs.this.pulse(pulse);
                }
            }
        );
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param pulse Pulse to convert
     * @return Bundle
     */
    private JaxbBundle pulse(final Pulse pulse) {
        return new JaxbBundle("pulse")
            .add("started", pulse.started().toString())
            .up()
            .add("spec", pulse.spec().asText())
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
                        .build(this.name, pulse.started().getTime())
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
