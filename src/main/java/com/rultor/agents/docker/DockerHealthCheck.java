/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.docker;

import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;

/**
 * Checks the Health of a Docker host and tries to recover Docker daemon
 * crashes.
 *
 * @since 1.63
 */
public final class DockerHealthCheck implements SuperAgent {

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Ctor.
     *
     * @param ssh Shell
     */
    public DockerHealthCheck(final Shell ssh) {
        this.shell = ssh;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        try (NullInputStream stdin = new NullInputStream(0L)) {
            new Shell.Safe(this.shell).exec(
                IOUtils.toString(
                    Objects.requireNonNull(
                        this.getClass().getResource("checkhost.sh")
                    ),
                    StandardCharsets.UTF_8
                ),
                stdin,
                Logger.stream(Level.INFO, this),
                Logger.stream(Level.WARNING, this)
            );
        }
    }
}
