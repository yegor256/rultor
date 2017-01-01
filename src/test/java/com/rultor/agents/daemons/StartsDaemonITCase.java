/**
 * Copyright (c) 2009-2017, rultor.com
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

import com.jcabi.immutable.ArrayMap;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.docker.StartsDockerDaemon;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Integration test for ${@link StartsDaemon}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.3.8
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class StartsDaemonITCase {

    /**
     * StartsDaemon can start a daemon.
     * @throws Exception In case of error.
     * @checkstyle ExecutableStatementCountCheck (50 lines)
     */
    @Test
    public void startsDaemon() throws Exception {
        try (
            final StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final Talk talk = StartsDaemonITCase.talk(start);
            MatcherAssert.assertThat(
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
                baos.toString(CharEncoding.UTF_8),
                Matchers.allOf(
                    Matchers.containsString("+ set -o pipefail"),
                    Matchers.containsString("+ date"),
                    Matchers.containsString("+ ls -al"),
                    Matchers.containsString("182f61268e6e6e6cd1a547be31fd8583")
                )
            );
            MatcherAssert.assertThat(
                new File(dir, "status").exists(), Matchers.is(false)
            );
        }
    }

    /**
     * StartsDaemon can deprecate default image (except one case, when
     * repo is is the actual Rultor repo: https://github.com/yegor256/rultor).
     * @throws IOException In case of error
     */
    @Test
    public void deprecatesDefaultImage() throws IOException {
        try (
            final StartsDockerDaemon start =
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
            MatcherAssert.assertThat(dir, matcher);
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
        Assume.assumeTrue(
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
