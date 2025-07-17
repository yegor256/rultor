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
import java.util.Objects;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;

/**
 * Remove old images from Docker.
 *
 * @since 1.57
 */
public final class DockerExec implements SuperAgent {

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Script to exec.
     */
    private final transient String script;

    /**
     * Ctor.
     * @param ssh Shell
     * @param scrpt Script
     */
    public DockerExec(final Shell ssh, final String scrpt) {
        this.shell = ssh;
        this.script = scrpt;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        new Shell.Safe(this.shell).exec(
            IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResource(this.script)),
                StandardCharsets.UTF_8
            ),
            new NullInputStream(0L),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
    }

}
