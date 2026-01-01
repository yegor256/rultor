/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.docker;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.rultor.StartsDockerDaemon;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link StartsDockerDaemon}.
 * @since 1.63
 */
final class StartsDockerDaemonTest {

    /**
     * StartsDockerDaemon can provide a working PfShell.
     * @throws Exception In case of failure
     */
    @Test
    void providesPfShell() throws Exception {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final PfShell shell = start.shell();
            MatcherAssert.assertThat(
                "Should login as root",
                shell.login(),
                Matchers.is("root")
            );
            final String key = shell.key();
            MatcherAssert.assertThat(
                "Should be RSA key",
                key,
                Matchers.allOf(
                    Matchers.startsWith("-----BEGIN RSA PRIVATE KEY-----"),
                    Matchers.endsWith("-----END RSA PRIVATE KEY-----")
                )
            );
            final Shell.Plain ssh = new Shell.Plain(
                new Ssh(shell.host(), shell.port(), shell.login(), shell.key())
            );
            MatcherAssert.assertThat(
                "Key should be placed in /root/.ssh/id_rsa",
                ssh.exec("cat /root/.ssh/id_rsa"),
                Matchers.containsString(key)
            );
        }
    }

}
