/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import java.io.IOException;

/**
 * Command to run in a given shell and working directory.
 *
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
            String.join(
                ShellCommand.SHELL_JOINER,
                String.format("cd %s", Ssh.escape(this.directory)),
                this.command
            )
        );
    }

}
