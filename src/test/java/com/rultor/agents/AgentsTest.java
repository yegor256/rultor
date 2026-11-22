/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import co.stateful.Sttc;
import co.stateful.mock.MkSttc;
import com.jcabi.github.GitHub;
import com.jcabi.github.RtGitHub;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.ssh.Shell;
import com.rultor.agents.docker.DockerHealthCheck;
import com.rultor.spi.Profile;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import com.yegor256.WeAreOnline;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for ${@link Agents}.
 *
 * @since 1.7
 */
final class AgentsTest {

    /**
     * Agents can process a talk.
     *
     * @throws Exception In case of error.
     */
    @Test
    @ExtendWith(WeAreOnline.class)
    void processesTalk() throws Exception {
        final Talk talk = new Talk.InFile();
        final GitHub github = new MkGitHub();
        final Sttc sttc = new MkSttc();
        final Profile profile = new Profile.Fixed();
        Assertions.assertDoesNotThrow(
            () -> new Agents(github, sttc)
                .agent(talk, profile).execute(talk)
        );
    }

    /**
     * Agents can wire DockerHealthCheck into the starter flow.
     *
     * @throws Exception In case of error.
     */
    @Test
    void starterIncludesDockerHealthCheck() throws Exception {
        MatcherAssert.assertThat(
            "Starter flow must include DockerHealthCheck",
            new Agents(new MkGitHub(), new MkSttc()).starter().toString(),
            Matchers.containsString(
                DockerHealthCheck.class.getSimpleName()
            )
        );
    }

    /**
     * Agents starter runs DockerHealthCheck and lets its {@link
     * IOException} escape.
     *
     * <p>DockerHealthCheck is wired into the starter without any
     * wrappers (no {@link SuperAgent.Quiet}, no {@link
     * SuperAgent.Disabled}), because a broken Docker host must abort
     * talk processing: the escaping IOException reaches Routine.safe(),
     * which records it in the pulse, see
     * <a href="https://github.com/yegor256/rultor/issues/1375">#1375</a>.
     * This test executes the actual starter with a fake failing shell:
     * checkhost.sh must run and its failure must reach the caller. The
     * test fails when the health check is removed, disabled or made
     * quiet.</p>
     *
     * @throws Exception In case of error.
     */
    @Test
    void starterRunsDockerHealthCheckAndPropagatesFailure() throws Exception {
        final Collection<String> executed = new ArrayList<>(1);
        MatcherAssert.assertThat(
            "The failure must carry the diagnostic of the health check",
            AgentsTest.healthCheckFailure(
                (command, stdin, stdout, stderr) -> {
                    executed.add(command);
                    throw new IOException(
                        "simulated failure of the Docker host"
                    );
                },
                executed
            ).getMessage(),
            Matchers.containsString("simulated failure of the Docker host")
        );
    }

    private static IOException healthCheckFailure(final Shell shell,
        final Collection<String> executed) throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("[]"))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_ACCEPTED, "{}"))
            .next(new MkAnswer.Simple("[]"))
            .start();
        try (container) {
            final GitHub github = new RtGitHub(container.home());
            final IOException raised = Assertions.assertThrows(
                IOException.class,
                () -> new Agents(github, new MkSttc(), shell)
                    .starter().execute(new Talks.InDir()),
                "DockerHealthCheck failure must escape the starter"
            );
            MatcherAssert.assertThat(
                "checkhost.sh must run as part of the starter flow",
                executed,
                Matchers.hasItem(Matchers.containsString("command -v docker"))
            );
            return raised;
        }
    }
}
