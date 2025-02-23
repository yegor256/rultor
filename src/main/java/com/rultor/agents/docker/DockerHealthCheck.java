/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.docker;

import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;

/**
 * Checks the Health of a Docker host and tries to recover Docker daemon
 * crashes.
 * @since 1.63
 * @todo #1041:30min Add DockerHealthCheck to the running SuperAgents.
 *  In doing so make sure that Rultor crashes throwing a meaningful exception
 *  as soon as DockerHealthCheck#execute throws and exception.
 *  #1041 has the details on the motivation behind this agent.
 */
public final class DockerHealthCheck implements SuperAgent {

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Ctor.
     * @param ssh Shell
     */
    public DockerHealthCheck(final Shell ssh) {
        this.shell = ssh;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        new Shell.Safe(this.shell).exec(
            IOUtils.toString(
                this.getClass().getResource("checkhost.sh"),
                StandardCharsets.UTF_8
            ),
            new NullInputStream(0L),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
    }

}
