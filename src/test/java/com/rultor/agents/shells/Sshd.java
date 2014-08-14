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
package com.rultor.agents.shells;

import com.jcabi.log.VerboseProcess;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Test SSHD daemon.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.3.8
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class Sshd {

    /**
     * Temp directory.
     */
    private final transient File dir;

    /**
     * Ctor.
     * @param path Directorty to work in
     */
    public Sshd(final File path) {
        this.dir = path;
    }

    /**
     * Get home dir.
     * @return Dir
     */
    public File home() {
        return this.dir;
    }

    /**
     * Get user name to login.
     * @return User name
     */
    public String login() {
        return new VerboseProcess(
            new ProcessBuilder().command("whoami")
        ).stdout().trim();
    }

    /**
     * Get private SSH key for login.
     * @return Key
     * @throws IOException If fails
     */
    public String key() throws IOException {
        return IOUtils.toString(
            this.getClass().getResourceAsStream("id_rsa"),
            CharEncoding.UTF_8
        );
    }

    /**
     * Start SSHD and return port number it is listening on.
     * @return Port number
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.DoNotUseThreads")
    public int start() throws IOException {
        final ServerSocket socket = new ServerSocket(0);
        final int port = socket.getLocalPort();
        socket.close();
        final File rsa = new File(this.dir, "host_rsa_key");
        IOUtils.copy(
            this.getClass().getResourceAsStream("ssh_host_rsa_key"),
            new FileOutputStream(rsa)
        );
        final File keys = new File(this.dir, "authorized");
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
            "-o", String.format("PidFile=%s", new File(this.dir, "pid")),
            "-o", "UsePAM=no",
            "-o", String.format("AuthorizedKeysFile=%s", keys),
            "-o", "StrictModes=no"
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
