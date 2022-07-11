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
package com.rultor.agents.docker;

import com.jcabi.ssh.Ssh;
import com.jcabi.ssh.Shell;
import com.rultor.StartsDockerDaemon;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for ${@link StartsDockerDaemon}.
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.63
 */
public final class StartsDockerDaemonTest {

    /**
     * StartsDockerDaemon can provide a working PfShell.
     * @throws Exception In case of failure
     */
    @Test
    public void providesPfShell() throws Exception {
        Assume.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        try (
            final StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final PfShell shell = start.shell();
            MatcherAssert.assertThat(
                shell.login(),
                Matchers.is("root")
            );
            final String key = shell.key();
            MatcherAssert.assertThat(
                key,
                Matchers.startsWith("-----BEGIN RSA PRIVATE KEY-----")
            );
            MatcherAssert.assertThat(
                key,
                Matchers.endsWith("-----END RSA PRIVATE KEY-----")
            );
            final Shell.Plain ssh = new Shell.Plain(
                new Ssh(shell.host(), shell.port(), shell.login(), shell.key())
            );
            MatcherAssert.assertThat(
                ssh.exec("cat /root/.ssh/id_rsa"),
                Matchers.containsString(key)
            );
        }
    }

}
