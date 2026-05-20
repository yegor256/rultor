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
     * StartsDockerDaemon can provide a PfShell that logs in as root.
     * @throws Exception In case of failure
     */
    @Test
    void providesPfShellLoginAsRoot() throws Exception {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        final StartsDockerDaemon start = new StartsDockerDaemon(Profile.EMPTY);
        try (start) {
            MatcherAssert.assertThat(
                "Should login as root",
                start.shell().login(),
                Matchers.is("root")
            );
        }
    }

    /**
     * StartsDockerDaemon can provide a PfShell with RSA key.
     * @throws Exception In case of failure
     */
    @Test
    void providesPfShellWithRsaKey() throws Exception {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        final StartsDockerDaemon start = new StartsDockerDaemon(Profile.EMPTY);
        try (start) {
            MatcherAssert.assertThat(
                "Should be RSA key",
                start.shell().key(),
                Matchers.allOf(
                    Matchers.startsWith("-----BEGIN RSA PRIVATE KEY-----"),
                    Matchers.endsWith("-----END RSA PRIVATE KEY-----")
                )
            );
        }
    }

    /**
     * StartsDockerDaemon places the key in /root/.ssh/id_rsa.
     * @throws Exception In case of failure
     */
    @Test
    void placesKeyInRootSsh() throws Exception {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        final StartsDockerDaemon start = new StartsDockerDaemon(Profile.EMPTY);
        try (start) {
            final PfShell shell = start.shell();
            MatcherAssert.assertThat(
                "Key should be placed in /root/.ssh/id_rsa",
                new Shell.Plain(
                    new Ssh(shell.host(), shell.port(), shell.login(), shell.key())
                ).exec("cat /root/.ssh/id_rsa"),
                Matchers.containsString(shell.key())
            );
        }
    }
}
