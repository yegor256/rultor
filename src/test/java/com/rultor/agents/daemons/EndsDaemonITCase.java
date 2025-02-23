/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.rultor.StartsDockerDaemon;
import com.rultor.Time;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringEndsWith;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for {@link EndsDaemon}.
 *
 * @since 1.2
 */
final class EndsDaemonITCase {

    /**
     * EndsDaemon should store highlighted stdout entry.
     * @throws IOException In case of error.
     */
    @Test
    void parsesHighlightedStdout() throws IOException {
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final Talk talk = new Talk.InFile();
            this.start(
                start,
                talk,
                String.format(
                    "some random\n%s%s\nother",
                    EndsDaemon.HIGHLIGHTS_PREFIX,
                    "text output"
                )
            );
            final Agent agent = new EndsDaemon();
            agent.execute(talk);
            MatcherAssert.assertThat(
                "Rultor prefix should be moved to highlights",
                talk.read(),
                XhtmlMatchers.hasXPaths(
                    "/talk/daemon/highlights",
                    "/talk/daemon/highlights[.='text output']"
                )
            );
        }
    }

    /**
     * EndsDaemon can read exit code.
     * @throws IOException In case of error.
     */
    @Test
    void readsExitCodeCorrectly() throws IOException {
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final Talk talk = new Talk.InFile();
            final PfShell sshd = this.start(start, talk, "");
            new Shell.Plain(
                new Ssh(sshd.host(), sshd.port(), sshd.login(), sshd.key())
            ).exec("echo '123' > /tmp/status");
            final Agent agent = new EndsDaemon();
            agent.execute(talk);
            MatcherAssert.assertThat(
                "Status code should be placed to daemon/code",
                talk.read(),
                XhtmlMatchers.hasXPath("/talk/daemon[code='123']")
            );
        }
    }

    /**
     * EndsDaemon can end the build in case of a broken profile.
     * @throws Exception On failure
     */
    @Test
    void exitsWhenProfileBroken() throws Exception {
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final Talk talk = new Talk.InFile();
            final PfShell sshd = this.start(start, talk, "");
            new Shell.Plain(
                new Ssh(sshd.host(), sshd.port(), sshd.login(), sshd.key())
            ).exec("echo '154' > /tmp/status");
            final Profile prof = Mockito.mock(Profile.class);
            final String exception = "This profile was broken!";
            Mockito.when(prof.read())
                .thenThrow(new Profile.ConfigException(exception));
            final Agent agent = new EndsDaemon();
            agent.execute(talk);
            MatcherAssert.assertThat(
                "Exception message should be placed in tail text",
                talk.read(),
                XhtmlMatchers.hasXPaths(
                    "/talk/daemon[code='154']",
                    "/talk/daemon/tail//text()[contains(., 'broken')]"
                )
            );
        }
    }

    /**
     * EndsDaemon can deprecate default image.
     * @throws IOException In case of error
     */
    @Test
    @Disabled
    void deprecatesDefaultImage() throws IOException {
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final Talk talk = new Talk.InFile();
            this.start(start, talk, "");
            new EndsDaemon().execute(talk);
            for (final String path
                : talk.read().xpath(
                "/p/entry[@key='merge']/entry[@key='script']"
                    )
            ) {
                if ("yegor256/rultor-image".equals(path)) {
                    final String dir = talk.read()
                        .xpath("/talk/daemon/dir/text()").get(0);
                    MatcherAssert.assertThat(
                        "Deprecation message should be printed",
                        dir,
                        Matchers.allOf(
                            StringContains.containsString(
                                "#### Deprecation Notice ####"
                            ),
                            StringEndsWith.endsWith("##############")
                        )
                    );
                }
            }
        }
    }

    /**
     * Start a talk.
     * @param start Docker daemon starter
     * @param talk Talk to start
     * @param stdout Std out
     * @return Home
     * @throws IOException In case of error.
     */
    private PfShell start(final StartsDockerDaemon start, final Talk talk,
        final String stdout) throws IOException {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        final PfShell sshd = start.shell();
        final int port = sshd.port();
        new Shell.Plain(
            new Ssh(sshd.host(), port, sshd.login(), sshd.key())
        ).exec(String.format("echo '%s' > /tmp/stdout", stdout));
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon")
                .attr("id", "abcd")
                .add("title").set("merge").up()
                .add("script").set("ls").up()
                .add("started").set(new Time().iso()).up()
                .add("dir").set("/tmp").up()
                .up()
                .add("shell").attr("id", "a1b2c3e3")
                .add("host").set("localhost").up()
                .add("port").set(Integer.toString(port)).up()
                .add("login").set(sshd.login()).up()
                .add("key").set(sshd.key()).up().up()
        );
        return sshd;
    }
}
