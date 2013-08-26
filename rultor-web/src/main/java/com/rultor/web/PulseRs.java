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
import com.rultor.spi.Arguments;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulse;
import com.rultor.spi.Repo;
import com.rultor.spi.SpecException;
import com.rultor.spi.Unit;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Single pulse.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/pulse/{name:[\\w\\-]+}/{date:\\d+}")
@Loggable(Loggable.DEBUG)
public final class PulseRs extends BaseRs {

    /**
     * Unit name.
     */
    private transient String name;

    /**
     * Pulse date.
     */
    private transient Time date;

    /**
     * Inject it from query.
     * @param unit Unit name
     */
    @PathParam("name")
    public void setName(@NotNull(message = "unit name is mandatory")
        final String unit) {
        this.name = unit;
    }

    /**
     * Inject it from query.
     * @param time PulseOfDrain time
     */
    @PathParam("date")
    public void setDate(@NotNull(message = "date is mandatory")
        final String time) {
        this.date = new Time(Long.parseLong(time));
    }

    /**
     * Get stream.
     * @return The JAX-RS response
     */
    @GET
    @Path("/stream")
    @Produces("text/plain; charset=UTF-8")
    public String stream() {
        try {
            return IOUtils.toString(this.pulse().stream());
        } catch (IOException ex) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUriBuilder()
                    .clone()
                    .path(DrainRs.class)
                    .build(this.name),
                String.format(
                    "Can't read this pulse: %s",
                    Exceptions.message(ex)
                ),
                Level.SEVERE
            );
        }
    }

    /**
     * Get pulse.
     * @return The pulse
     */
    private Pulse pulse() {
        final Unit unit;
        try {
            unit = this.user().units().get(this.name);
        } catch (NoSuchElementException ex) {
            throw this.flash().redirect(this.uriInfo().getBaseUri(), ex);
        }
        return new Pulse() {
            @Override
            public String xembly() throws IOException {
                throw new UnsupportedOperationException();
            }
            @Override
            public InputStream stream() throws IOException {
                return PulseRs.this.read(unit);
            }
        };
    }

    /**
     * Read stream of the drain.
     * @param unit Unit to read from
     * @return Stream
     * @throws IOException If fails
     */
    private InputStream read(final Unit unit) throws IOException {
        try {
            return Drain.Source.class.cast(
                new Repo.Cached(
                    this.repo(), this.user(), unit.spec()
                ).get().instantiate(
                    this.users(),
                    new Arguments(
                        new Work.Simple(
                            this.user().urn(),
                            this.name,
                            this.date
                        ),
                        new Wallet.Empty()
                    )
                )
            ).drain().read();
        } catch (SpecException ex) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format(
                    "Can't instantiate drain of \"%s\": %s",
                    this.name,
                    Exceptions.message(ex)
                ),
                Level.SEVERE
            );
        }
    }

}
