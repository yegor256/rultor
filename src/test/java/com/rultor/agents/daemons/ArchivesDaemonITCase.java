/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.s3.fake.FkBucket;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.rultor.StartsDockerDaemon;
import com.rultor.Time;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xembly.Directives;

/**
 * Tests for ${@link ArchivesDaemon}.
 *
 * @since 1.23
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class ArchivesDaemonITCase {
    /**
     * ArchivesDaemon can archive a daemon.
     * @param temp Temporary directory
     * @throws Exception In case of error.
     */
    @Test
    void archivesDaemon(@TempDir final Path temp) throws Exception {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final PfShell shell = start.shell();
            final int port = shell.port();
            new Shell.Plain(
                new Ssh(shell.host(), port, shell.login(), shell.key())
            ).exec("echo 'some output' > /tmp/stdout");
            final Talk talk = new Talk.InFile();
            talk.modify(
                new Directives().xpath("/talk")
                    .add("daemon")
                    .attr("id", "abcd")
                    .add("title").set("merge").up()
                    .add("script").set("empty").up()
                    .add("dir").set("/tmp").up()
                    .add("code").set("-7").up()
                    .add("started").set(new Time().iso()).up()
                    .add("ended").set(new Time().iso()).up().up()
                    .add("shell").attr("id", "a1b2c3e3")
                    .add("host").set("localhost").up()
                    .add("port").set(Integer.toString(port)).up()
                    .add("login").set(shell.login()).up()
                    .add("key").set(shell.key()).up().up()
            );
            final Agent agent = new ArchivesDaemon(
                new FkBucket(temp, "test")
            );
            agent.execute(talk);
            MatcherAssert.assertThat(
                "archive tag should be created with bucket name",
                talk.read(),
                XhtmlMatchers.hasXPaths(
                    "/talk[not(daemon)]",
                    // @checkstyle LineLength (1 line)
                    "/talk/archive/log[@id='abcd' and starts-with(.,'s3://test/')]",
                    "/talk/archive/log[@id='abcd' and @title]"
                )
            );
        }
    }

}
