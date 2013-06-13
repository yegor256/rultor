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
import com.rultor.spi.Pulse;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Single pulse.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/pls")
@Loggable(Loggable.DEBUG)
public final class PulseRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_NAME = "name";

    /**
     * Query param.
     */
    public static final String QUERY_DATE = "date";

    /**
     * Query param.
     */
    public static final String QUERY_POSITION = "p";

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
     * Pulse position in list.
     */
    private transient int position;

    /**
     * Start.
     */
    private transient Long start;

    /**
     * Stop.
     */
    private transient Long stop;

    /**
     * Inject it from query.
     * @param unit Unit name
     */
    @QueryParam(PulseRs.QUERY_NAME)
    public void setName(@NotNull final String unit) {
        this.name = unit;
    }

    /**
     * Inject it from query.
     * @param time Pulse time
     */
    @QueryParam(PulseRs.QUERY_DATE)
    public void setDate(@NotNull final String time) {
        this.date = new Date(Long.parseLong(time));
    }

    /**
     * Inject it from query.
     * @param pos Pulse position in list
     */
    @QueryParam(PulseRs.QUERY_POSITION)
    public void setPosition(@NotNull final String pos) {
        this.position = Integer.parseInt(pos);
    }

    /**
     * Inject it from query.
     * @param time Pulse time
     */
    @QueryParam(PulseRs.QUERY_START)
    public void setStart(final String time) {
        if (time == null) {
            this.start = 0L;
        } else {
            this.start = Long.parseLong(time);
        }
    }

    /**
     * Inject it from query.
     * @param time Pulse time
     */
    @QueryParam(PulseRs.QUERY_STOP)
    public void setStop(final String time) {
        if (time == null) {
            this.stop = Long.MAX_VALUE;
        } else {
            this.stop = Long.parseLong(time);
        }
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
        assert this.start != null;
        assert this.stop != null;
        return Response.ok().entity(this.pulse().read()).build();
    }

    /**
     * Get pulse.
     * @return The pulse
     */
    private Pulse pulse() {
        final List<Pulse> pulses = this.user().units().get(this.name).pulses();
        if (this.position > pulses.size()) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "Pulse #%d is out of boundary",
                    this.position
                ),
                Level.SEVERE
            );
        }
        final Pulse pulse = pulses.get(this.position);
        if (!pulse.started().equals(this.date)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUriBuilder()
                    .clone()
                    .path(PulsesRs.class)
                    .queryParam(PulsesRs.QUERY_NAME, "{x}")
                    .build(this.name),
                "The list was refreshed, try again",
                Level.INFO
            );
        }
        return pulse;
    }

}
