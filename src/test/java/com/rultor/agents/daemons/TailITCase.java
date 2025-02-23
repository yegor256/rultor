/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.rultor.StartsDockerDaemon;
import com.rultor.Time;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link Tail}.
 *
 * @since 1.62
 */
final class TailITCase {

    /**
     * Tail can convert non UTF-8 chars in StdOut to UTF-8.
     * @throws Exception In case of error.
     */
    @Test
    void tailsNonUtf() throws Exception {
        Assumptions.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        try (
            StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final PfShell sshd = start.shell();
            final String clean = "some output";
            new Shell.Plain(
                new Ssh(sshd.host(), sshd.port(), sshd.login(), sshd.key())
            ).exec(String.format("echo '%s\u00ea' > /tmp/stdout", clean));
            final Talk talk = new Talk.InFile();
            final String hash = "a1b5c3e3";
            final String key = "id";
            talk.modify(
                new Directives().xpath("/talk")
                    .add("daemon")
                    .attr(key, hash)
                    .add("title").set("tail").up()
                    .add("script").set("empty").up()
                    .add("dir").set("/tmp").up()
                    .add("code").set("-7").up()
                    .add("started").set(new Time().iso()).up()
                    .add("ended").set(new Time().iso()).up().up()
                    .add("shell").attr(key, hash)
                    .add("host").set(sshd.host()).up()
                    .add("port").set(Integer.toString(sshd.port())).up()
                    .add("login").set(sshd.login()).up()
                    .add("key").set(sshd.key()).up().up()
            );
            MatcherAssert.assertThat(
                "SSH output should be in the Tail",
                IOUtils.toString(
                    new Tail(talk.read(), hash).read(),
                    StandardCharsets.UTF_8
                ),
                Matchers.is(String.format("%sê\n", clean))
            );
        }
    }

}
