/**
 * Copyright (c) 2009-2018, rultor.com
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
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import java.io.IOException;
import org.cactoos.text.JoinedText;

/**
 * Command to run in a given shell and working directory.
 *
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.62
 */
@Immutable
final class ShellCommand {

    /**
     * Join shell commands with this string.
     */
    private static final String SHELL_JOINER = " && ";

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Shell command to run.
     */
    private final transient String command;

    /**
     * Shell command to run.
     */
    private final transient String directory;

    /**
     * Ctor.
     * @param shll Shell
     * @param dir String working directory
     * @param cmd String command to run
     */
    ShellCommand(final Shell shll, final String dir, final String cmd) {
        this.shell = shll;
        this.directory = dir;
        this.command = cmd;
    }

    /**
     * Executes the command.
     * @return Stdout
     * @throws IOException If fails
     */
    public String exec() throws IOException {
        return new Shell.Plain(new Shell.Safe(this.shell)).exec(
            new JoinedText(
                ShellCommand.SHELL_JOINER,
                String.format("cd %s", SSH.escape(this.directory)),
                this.command
            ).asString()
        );
    }

}
