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
package com.rultor.shell.bash;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.shell.Batch;
import com.rultor.shell.Shell;
import com.rultor.shell.Shells;
import com.rultor.snapshot.XemblyLine;
import com.rultor.tools.Time;
import com.rultor.tools.Vext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directives;

/**
 * Bash batch.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "shells", "script" })
@Loggable(Loggable.DEBUG)
public final class Bash implements Batch {

    /**
     * Shells.
     */
    private final transient Shells shells;

    /**
     * Script to execute.
     */
    private final transient Vext script;

    /**
     * Public ctor.
     * @param shls Shells
     * @param scrt Script to run there, Apache Velocity template
     */
    public Bash(
        @NotNull(message = "shells can't be NULL") final Shells shls,
        @NotNull(message = "script can't be NULL") final String scrt) {
        this.shells = shls;
        this.script = new Vext(scrt);
    }

    /**
     * Public ctor.
     * @param shls Shells
     * @param lines Script lines
     */
    public Bash(final Shells shls, final Collection<String> lines) {
        this(shls, StringUtils.join(lines, " && "));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public int exec(
        @NotNull(message = "args can't be NULL") final Map<String, Object> args,
        @NotNull(message = "stream can't be NULL") final OutputStream output)
        throws IOException {
        final Shell shell = this.shells.acquire();
        final String command = this.script.print(args);
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        final int code;
        try {
            code = shell.exec(
                command,
                IOUtils.toInputStream(""),
                new TeeOutputStream(output, Logger.stream(Level.INFO, this)),
                new TeeOutputStream(
                    output,
                    new TeeOutputStream(
                        stderr,
                        Logger.stream(Level.WARNING, this)
                    )
                )
            );
        } finally {
            shell.close();
        }
        if (code != 0) {
            new XemblyLine(
                new Directives()
                    .xpath("/snapshot")
                    .addIfAbsent("steps")
                    .add("step").add("summary")
                    .set(String.format("bash error code #%d", code)).up()
                    .add("finish").set(new Time().toString()).up()
                    .add("level").set(Level.SEVERE.toString()).up()
                    .add("exception")
                    .add("cause").set(Integer.toString(code)).up()
                    .add("stacktrace").set(stderr.toString(CharEncoding.UTF_8))
            ).log();
        }
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "bash batch `%[text]s` through %s",
            this.script, this.shells
        );
    }

}
