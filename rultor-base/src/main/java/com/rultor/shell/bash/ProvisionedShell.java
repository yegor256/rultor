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
import com.rultor.shell.Shell;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Shell executing script prepended by another script.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = {"shell", "script" })
@Loggable(Loggable.DEBUG)
public final class ProvisionedShell implements Shell {

    /**
     * Underlying shell.
     */
    private final transient Shell shell;

    /**
     * Script to prepend with.
     */
    private final transient String script;

    /**
     * Public ctor.
     * @param scrt Script to prepend with
     * @param shl Underlying Shell
     */
    public ProvisionedShell(
        @NotNull(message = "script can't be NULL") final String scrt,
        @NotNull(message = "shell can't be NULL") final Shell shl) {
        this.script = scrt;
        this.shell = shl;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (8 lines)
     */
    @Override
    public int exec(
        @NotNull(message = "command can't be NULL") final String command,
        @NotNull(message = "stdin can't be NULL") final InputStream stdin,
        @NotNull(message = "stdout can't be NULL") final OutputStream stdout,
        @NotNull(message = "stderr can't be NULL") final OutputStream stderr
    ) throws IOException {
        return this.shell.exec(
            new StringBuilder(this.script)
                .append(" && ")
                .append(command)
                .toString(),
            stdin, stdout, stderr
        );
    }

    @Override
    public void badge(final String name, final String value) {
        this.shell.badge(name, value);
    }

    @Override
    public void close() throws IOException {
        this.shell.close();
    }
}
