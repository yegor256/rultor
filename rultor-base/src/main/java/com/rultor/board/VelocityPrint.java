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
package com.rultor.board;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Pre-renders announcements using Apache Velocity template.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "board", "template" })
@Loggable(Loggable.DEBUG)
public final class VelocityPrint implements Billboard {

    /**
     * Original board.
     */
    private final transient Billboard board;

    /**
     * Velocity template.
     */
    private final transient String template;

    /**
     * Name of argument to inject into announcement.
     */
    private final transient String argument;

    /**
     * Public ctor.
     * @param arg Name of the argument
     * @param brd Original board
     * @param tmpl Velocity template
     */
    public VelocityPrint(
        @NotNull(message = "argument can't be NULL") final String arg,
        @NotNull(message = "template can't be NULL") final String tmpl,
        @NotNull(message = "board can't be NULL") final Billboard brd) {
        this.argument = arg;
        this.board = brd;
        this.template = tmpl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s with Velocity layout",
            this.board
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(@NotNull(message = "announcement can't be NULL")
        final Announcement anmt) throws IOException {
        final StringWriter writer = new StringWriter();
        final Context context = new VelocityContext();
        for (Map.Entry<String, Object> entry : anmt.args().entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        final VelocityEngine engine = new VelocityEngine();
        engine.setProperty(
            RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
            "org.apache.velocity.runtime.log.Log4JLogChute"
        );
        engine.setProperty(
            "runtime.log.logsystem.log4j.logger",
            "org.apache.velocity"
        );
        engine.init();
        final boolean success = engine.evaluate(
            context, writer,
            this.getClass().getName(), this.template
        );
        Validate.isTrue(success, "failed to compile VTL");
        this.board.announce(anmt.with(this.argument, writer.toString()));
    }

}
