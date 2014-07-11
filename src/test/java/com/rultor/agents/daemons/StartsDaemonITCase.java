/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.jcabi.log.VerboseProcess;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xembly.Directives;

/**
 * Integration test for ${@link StartsDaemon}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.3.8
 */
public final class StartsDaemonITCase {

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * StartsDaemon can start a daemon.
     * @throws Exception In case of error.
     */
    @Test
    public void startsDaemon() throws Exception {
        final int port = this.sshd();
        final String key = IOUtils.toString(
            this.getClass().getResourceAsStream("id_rsa")
        );
        final String whoami = new VerboseProcess(
            new ProcessBuilder().command("whoami")
        ).stdout().trim();
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("shell").attr("id", "abcdef")
                .add("host").set("localhost").up()
                .add("port").set(Integer.toString(port)).up()
                .add("login").set(whoami).up()
                .add("key").set(key).up().up()
                .add("daemon").attr("id", "fedcba")
                .add("script").set("ls -al")
        );
        final Agent agent = new StartsDaemon(new Profile.Fixed());
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath("/talk/daemon[started and dir]")
        );
    }

    /**
     * Start SSHD and return port number it is listening on.
     * @return Port number
     * @throws IOException If fails
     */
    private int sshd() throws IOException {
        final ServerSocket socket = new ServerSocket(0);
        final int port = socket.getLocalPort();
        socket.close();
        final File rsa = this.temp.newFile();
        IOUtils.copy(
            this.getClass().getResourceAsStream("ssh_host_rsa_key"),
            new FileOutputStream(rsa)
        );
        final File keys = this.temp.newFile();
        IOUtils.copy(
            this.getClass().getResourceAsStream("authorized_keys"),
            new FileOutputStream(keys)
        );
        new VerboseProcess(
            new ProcessBuilder().command(
                "chmod", "600",
                keys.getAbsolutePath(),
                rsa.getAbsolutePath()
            )
        ).stdout();
        final Process proc = new ProcessBuilder().command(
            "/usr/sbin/sshd",
            "-p",
            Integer.toString(port),
            "-h",
            rsa.getAbsolutePath(),
            "-D",
            "-e",
            "-o",
            String.format("PidFile=%s", this.temp.newFile()),
            "-o",
            "UsePAM=no",
            "-o",
            String.format("AuthorizedKeysFile=%s", keys)
        ).start();
        new Thread(
            new Runnable() {
                @Override
                public void run() {
                    new VerboseProcess(proc).stdout();
                }
            }
        ).start();
        return port;
    }

}
