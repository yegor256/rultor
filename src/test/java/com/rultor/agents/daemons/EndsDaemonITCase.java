/**
 * Copyright (c) 2009-2016, rultor.com
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
import com.jcabi.ssh.SSHD;
import com.rultor.Time;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xembly.Directives;

/**
 * Tests for {@link EndsDaemon}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class EndsDaemonITCase {
    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * EndsDaemon should store highlighted stdout entry.
     * @throws IOException In case of error.
     */
    @Test
    public void parsesHighlightedStdout() throws IOException {
        final Talk talk = new Talk.InFile();
        this.start(
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

    /**
     * EndsDaemon can read exit code.
     * @throws IOException In case of error.
     */
    @Test
    public void readsExitCodeCorrectly() throws IOException {
        final Talk talk = new Talk.InFile();
        final File home = this.start(talk, "");
        FileUtils.write(new File(home.getAbsolutePath(), "status"), "123");
        final Agent agent = new EndsDaemon();
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath("/talk/daemon[code='123']")
        );
    }

    /**
     * Start a talk.
     * @param talk Talk to start
     * @param stdout Std out
     * @return Home
     * @throws IOException In case of error.
     */
    private File start(final Talk talk, final String stdout)
        throws IOException {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);
        final SSHD sshd = new SSHD(this.temp.newFolder());
        final int port = sshd.port();
        final File home = new File(sshd.home(), "test-home");
        FileUtils.forceMkdir(home);
        FileUtils.write(new File(home.getAbsolutePath(), "stdout"), stdout);
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon")
                    // @checkstyle MultipleStringLiterals (1 line)
                .attr("id", "abcd")
                .add("title").set("merge").up()
                .add("script").set("ls").up()
                .add("started").set(new Time().iso()).up()
                .add("dir").set(home.getAbsolutePath()).up()
                .up()
                .add("shell").attr("id", "a1b2c3e3")
                .add("host").set("localhost").up()
                .add("port").set(Integer.toString(port)).up()
                .add("login").set(sshd.login()).up()
                .add("key").set(sshd.key()).up().up()
        );
        return home;
    }
}
