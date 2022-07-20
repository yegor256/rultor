/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class EndsDaemonITCase {

    /**
     * EndsDaemon should store highlighted stdout entry.
     * @throws IOException In case of error.
     */
    @Test
    public void parsesHighlightedStdout() throws IOException {
        try (
            final StartsDockerDaemon start =
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
    public void readsExitCodeCorrectly() throws IOException {
        try (
            final StartsDockerDaemon start =
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
    public void exitsWhenProfileBroken() throws Exception {
        try (
            final StartsDockerDaemon start =
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
    public void deprecatesDefaultImage() throws IOException {
        try (
            final StartsDockerDaemon start =
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
                        dir,
                        StringContains.containsString(
                            "#### Deprecation Notice ####"
                        )
                    );
                    MatcherAssert.assertThat(
                        dir,
                        StringEndsWith.endsWith("##############")
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
                    // @checkstyle MultipleStringLiterals (1 line)
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
