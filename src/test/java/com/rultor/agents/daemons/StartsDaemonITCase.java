/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.StartsDockerDaemon;
import com.rultor.agents.shells.PfShell;
import com.rultor.agents.shells.TalkShells;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.input.NullInputStream;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Integration test for ${@link StartsDaemon}.
 * @since 1.3.8
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
final class StartsDaemonITCase {

    /**
     * StartsDaemon can start a daemon.
     * @throws Exception In case of error.
     * @checkstyle ExecutableStatementCountCheck (50 lines)
     */
    @Test
    void startsDaemon() throws Exception {
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final Talk talk = StartsDaemonITCase.talk(start);
            MatcherAssert.assertThat(
                "started tag should be added with start time",
                talk.read(),
                XhtmlMatchers.hasXPaths(
                    "/talk/daemon[started and dir]",
                    "/talk/daemon[ends-with(started,'Z')]"
                )
            );
            final String dir = talk.read().xpath("/talk/daemon/dir/text()")
                .get(0);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TimeUnit.SECONDS.sleep(2L);
            new Shell.Safe(new TalkShells(talk.read()).get()).exec(
                String.format("cat %s/stdout", dir),
                new NullInputStream(0L),
                baos, baos
            );
            MatcherAssert.assertThat(
                "Start script should be send to daemon",
                baos.toString(StandardCharsets.UTF_8),
                Matchers.allOf(
                    Matchers.containsString("+ set -o pipefail"),
                    Matchers.containsString("+ date"),
                    Matchers.containsString("+ ls -al"),
                    Matchers.containsString("182f61268e6e6e6cd1a547be31fd8583")
                )
            );
            MatcherAssert.assertThat(
                "status file should not be created",
                new File(dir, "status").exists(),
                Matchers.is(false)
            );
        }
    }

    /**
     * StartsDaemon can deprecate default image (except one case, when
     * repo is is the actual Rultor repo: https://github.com/yegor256/rultor).
     * @throws IOException In case of error
     */
    @Test
    void deprecatesDefaultImage() throws IOException {
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final Talk talk = StartsDaemonITCase.talk(start);
            final XML xml = talk.read();
            final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
            final List<String> repos = xml.xpath("/wire/github-repo/text()");
            final String notice = "#### Deprecation Notice ####";
            final Matcher<String> matcher;
            final String rultor = "yegor256/rultor";
            if ((repos.isEmpty() || !rultor.equals(repos.get(0)))
                && xml.xpath(
                "/p/entry[@key='merge']/entry[@key='script']"
            ).contains(rultor)
                ) {
                matcher = StringStartsWith.startsWith(notice);
            } else {
                matcher = Matchers.not(StringStartsWith.startsWith(notice));
            }
            MatcherAssert.assertThat(
                "Deprecation message in case of default image should be printed",
                dir,
                matcher
            );
        }
    }

    /**
     * Creates a Talk object with basic parameters.
     * @param start Docker daemon starter
     * @return The basic Talk object for testing
     * @throws IOException In case of error
     */
    private static Talk talk(final StartsDockerDaemon start)
        throws IOException {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        final PfShell shell = start.shell();
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("shell").attr("id", "abcdef")
                .add("host").set("localhost").up()
                .add("port").set(Integer.toString(shell.port())).up()
                .add("login").set(shell.login()).up()
                .add("key").set(shell.key()).up().up()
                .add("daemon").attr("id", "fedcba")
                .add("title").set("some operation").up()
                .add("script").set("ls -al; md5sum file.bin; sleep 50000")
        );
        final Profile profile = Mockito.mock(Profile.class);
        Mockito.doReturn(new XMLDocument("<p/>")).when(profile).read();
        Mockito.doReturn(
            new ArrayMap<String, InputStream>().with(
                "file.bin",
                new ByteArrayInputStream(
                    // @checkstyle MagicNumber (1 line)
                    new byte[] {0, 1, 7, 8, 9, 10, 13, 20}
                )
            )
        ).when(profile).assets();
        final Agent agent = new StartsDaemon(profile);
        agent.execute(talk);
        return talk;
    }
}
