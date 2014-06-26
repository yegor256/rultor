/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.tools;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.StringWriter;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Velocity text.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "template")
@Loggable(Loggable.DEBUG)
public final class Vext {

    /**
     * Template encapsulated.
     */
    private final transient String template;

    /**
     * Public ctor.
     * @param text Template to encapsulate
     */
    public Vext(final String text) {
        this.template = text;
    }

    @Override
    public String toString() {
        return Logger.format("`%[text]s`", this.template);
    }

    /**
     * Get velocity source.
     * @return The text/template
     */
    public String velocity() {
        return this.template;
    }

    /**
     * Print using these arguments.
     * @param <T> Type of values in the arguments
     * @param args Arguments
     * @return Text printed
     */
    public <T> String print(@NotNull(message = "args can't be NULL")
        final Map<String, T> args) {
        final StringWriter writer = new StringWriter();
        final Context context = new VelocityContext();
        for (final Map.Entry<String, T> entry : args.entrySet()) {
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
        try {
            final boolean success = engine.evaluate(
                context, writer,
                this.getClass().getName(), this.template
            );
            Validate.isTrue(success, "failed to compile VTL");
        } catch (final ParseErrorException ex) {
            throw new VelocityException(
                String.format(
                    "Invalid template: %s. %s",
                    this.template,
                    ex.getMessage()
                ),
                ex
            );
        }
        return writer.toString();
    }

}
