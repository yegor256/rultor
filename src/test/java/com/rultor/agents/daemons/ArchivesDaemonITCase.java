/**
 * Copyright (c) 2009-2024 Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xembly.Directives;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for ${@link ArchivesDaemon}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.23
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class ArchivesDaemonITCase {

    /**
     * ArchivesDaemon can archive a daemon.
     * @throws Exception In case of error.
     */
    @Test
    public void archivesDaemon(@TempDir Path tempDir) throws Exception {
        assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        try (
            final StartsDockerDaemon start =
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
                new FkBucket(tempDir, "test")
            );
            agent.execute(talk);
            MatcherAssert.assertThat(
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
