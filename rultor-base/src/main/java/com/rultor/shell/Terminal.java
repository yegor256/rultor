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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;

/**
 * Simplified terminal.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "shell")
@Loggable(Loggable.DEBUG)
public final class Terminal {

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Public ctor.
     * @param shl Shell to use
     */
    public Terminal(@NotNull(message = "shell can't be NULL") final Shell shl) {
        this.shell = shl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("terminal to %s", this.shell);
    }

    /**
     * Escape argument.
     * @param arg Argument
     * @return Escaped and bash-safe
     * @todo #34 This implementation is extremely bad
     */
    public static String escape(@NotNull(message = "argument can't be NULL")
        final String arg) {
        return new StringBuilder()
            .append('"')
            .append(
                arg.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("$", "\\$")
                    .replace("!", "\"'!'\"")
                    .replace("`", "\\`")
            )
            .append('"')
            .toString();
    }

    /**
     * Run this command and return its output (fail on non-zero exit code).
     * @param command The command
     * @return Output stream
     * @throws IOException If some IO problem inside
     */
    public String exec(@NotNull(message = "command can't be NULL")
        final String command) throws IOException {
        return this.exec(command, "");
    }

    /**
     * Run this command and return its output (fail on non-zero exit code).
     * @param command The command
     * @param stdin Input stream
     * @return Output stream
     * @throws IOException If some IO problem inside
     */
    public String exec(
        @NotNull(message = "command can't be NULL") final String command,
        @NotNull(message = "stdin can't be NULL") final String stdin)
        throws IOException {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        final int code = this.shell.exec(
            command,
            IOUtils.toInputStream(stdin, CharEncoding.UTF_8),
            new TeeOutputStream(stdout, Logger.stream(Level.INFO, this)),
            new TeeOutputStream(stderr, Logger.stream(Level.WARNING, this))
        );
        if (code != 0) {
            throw new IOException(
                Logger.format(
                    // @checkstyle LineLength (1 line)
                    "non-zero exit code %d after executing `%s` at %s, stdout='%[text]s', stderr='%s'",
                    code, command, this.shell,
                    stdout.toString(CharEncoding.UTF_8),
                    stderr.toString(CharEncoding.UTF_8)
                )
            );
        }
        return stdout.toString(CharEncoding.UTF_8);
    }

}
