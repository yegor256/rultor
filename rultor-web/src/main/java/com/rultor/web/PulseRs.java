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
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Single pulse.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/pulse/{name:[\\w\\-]+}/{date:\\d+}")
@Loggable(Loggable.DEBUG)
public final class PulseRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_START = "start";

    /**
     * Query param.
     */
    public static final String QUERY_STOP = "stop";

    /**
     * Unit name.
     */
    private transient String name;

    /**
     * Pulse date.
     */
    private transient Date date;

    /**
     * Inject it from query.
     * @param unit Unit name
     */
    @PathParam("name")
    public void setName(@NotNull final String unit) {
        this.name = unit;
    }

    /**
     * Inject it from query.
     * @param time Pulse time
     */
    @PathParam("date")
    public void setDate(@NotNull final String time) {
        this.date = new Date(Long.parseLong(time));
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response index() throws Exception {
        return new PageBuilder()
            .stylesheet("/xsl/pulse.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("unit", this.name))
            .append(
                new JaxbBundle(
                    "pulse",
                    Long.toString(this.date.getTime())
                )
            )
            .append(
                new JaxbBundle(
                    "spec",
                    this.pulse().spec().asText()
                )
            )
            .append(
                new JaxbBundle(
                    "date",
                    DateFormatUtils.formatUTC(
                        this.date, "yyyy-MM-dd'T'HH:mm'Z'"
                    )
                )
            )
            .append(
                new JaxbBundle("stages").add(
                    new JaxbBundle.Group<Stage>(this.pulse().stages()) {
                        @Override
                        public JaxbBundle bundle(final Stage stage) {
                            return PulseRs.this.stage(stage);
                        }
                    }
                )
            )
            .render()
            .build();
    }

    /**
     * Get stream.
     * @param start Start moment to render
     * @param stop Stop moment to render
     * @return The JAX-RS response
     * @throws Exception If some problem inside
     */
    @GET
    @Path("/stream")
    @Produces(MediaType.TEXT_PLAIN)
    public String stream(@QueryParam(PulseRs.QUERY_START) final String start,
        @QueryParam(PulseRs.QUERY_STOP) final String stop) throws Exception {
        return new StringBuilder()
            .append("start: ")
            .append(start)
            .append(CharUtils.LF)
            .append("stop: ")
            .append(stop)
            .append(CharUtils.LF)
            .append(IOUtils.toString(this.pulse().read()))
            .toString();
    }

    /**
     * Get pulse.
     * @return The pulse
     */
    private Pulse pulse() {
        final Unit unit = this.user().units().get(this.name);
        if (unit == null) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("Unit '%s' doesn't exist", this.name),
                Level.SEVERE
            );
        }
        final Pulse pulse = unit.pulses().get(this.date);
        if (pulse == null) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUriBuilder()
                    .clone()
                    .path(PulsesRs.class)
                    .build(this.name),
                String.format(
                    "Pulse '%d' doesn't exist any more",
                    this.date.getTime()
                ),
                Level.SEVERE
            );
        }
        return pulse;
    }

    /**
     * Convert stage to JaxbBundle.
     * @param stage Stage to convert
     * @return Bundle
     */
    private JaxbBundle stage(final Stage stage) {
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
                        .build(this.name, this.date.getTime())
                )
            );
    }

}
