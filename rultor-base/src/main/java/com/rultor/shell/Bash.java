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
package com.rultor.shell;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.spi.Signal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

/**
 * Bash batch.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "shells", "script", "prerequisites" })
@Loggable(Loggable.DEBUG)
public final class Bash implements Batch {

    /**
     * Shells.
     */
    private final transient Shells shells;

    /**
     * Script to execute.
     */
    private final transient String script;

    /**
     * Prerequisites.
     */
    private final transient Object[][] prerequisites;

    /**
     * Public ctor.
     * @param shls Shells
     * @param scrt Script to run there, Apache Velocity template
     */
    public Bash(@NotNull final Shells shls, @NotNull final String scrt) {
        this(shls, scrt, new ConcurrentHashMap<String, Object>(0));
    }

    /**
     * Public ctor.
     * @param shls Shells
     * @param scrt Script to run there, Apache Velocity template
     * @param map Prerequisites
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Bash(@NotNull final Shells shls, @NotNull final String scrt,
        @NotNull final Map<String, Object> map) {
        this.shells = shls;
        this.script = scrt;
        this.prerequisites = new Object[map.size()][];
        int idx = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            this.prerequisites[idx] = new Object[] {
                entry.getKey(), entry.getValue(),
            };
            ++idx;
        }
    }

    /**
     * Public ctor.
     * @param shls Shells
     * @param lines Script lines
     * @param map Prerequisites
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Bash(@NotNull final Shells shls,
        @NotNull final Collection<String> lines,
        @NotNull final Map<String, Object> map) {
        this(shls, StringUtils.join(lines, " && "), map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public int exec(@NotNull final Map<String, Object> args,
        @NotNull final OutputStream output) throws IOException {
        final Shell shell = this.shells.acquire();
        Signal.log(Signal.Mnemo.SUCCESS, "%s acquired", shell);
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        int code;
        try {
            for (Object[] pair : this.prerequisites) {
                shell.exec(
                    String.format("cat > %s", pair[0]),
                    Bash.toInputStream(pair[1]),
                    Logger.stream(Level.INFO, this),
                    Logger.stream(Level.WARNING, this)
                );
            }
            code = shell.exec(
                this.compile(args),
                IOUtils.toInputStream(""),
                new TeeOutputStream(stdout, Logger.stream(Level.INFO, this)),
                new TeeOutputStream(stderr, Logger.stream(Level.WARNING, this))
            );
            IOUtils.copy(
                new ByteArrayInputStream(stdout.toByteArray()), output
            );
        } finally {
            output.close();
            shell.close();
        }
        Signal.log(Signal.Mnemo.SUCCESS, "Batch executed");
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "bash batch `%[text]s` through %s",
            this.script,
            this.shells
        );
    }

    /**
     * Compile template using arguments.
     * @param args Arguments
     * @return Compiled script
     */
    private String compile(final Map<String, Object> args) {
        final StringWriter writer = new StringWriter();
        final Context context = new VelocityContext();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        final boolean success = Velocity.evaluate(
            context, writer,
            this.getClass().getName(), this.script
        );
        Validate.isTrue(success, "failed to compile VTL");
        return writer.toString();
    }

    /**
     * Convert it to input stream.
     * @param object Object
     * @return Input stream
     */
    private static InputStream toInputStream(final Object object) {
        final InputStream stream;
        if (object instanceof InputStream) {
            stream = InputStream.class.cast(object);
        } else {
            stream = IOUtils.toInputStream(object.toString());
        }
        return stream;
    }

}
