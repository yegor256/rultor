/**
 * Copyright (c) 2009-2018, rultor.com
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

import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import com.rultor.Time;
import com.rultor.agents.docker.StartsDockerDaemon;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link Tail}.
 *
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.62
 */
public final class TailITCase {

    /**
     * Tail can convert non UTF-8 chars in StdOut to UTF-8.
     * @throws Exception In case of error.
     */
    @Test
    public void tailsNonUtf() throws Exception {
        Assume.assumeTrue(
            "true".equalsIgnoreCase(System.getProperty("run-docker-tests"))
        );
        try (
            final StartsDockerDaemon start =
                new StartsDockerDaemon(Profile.EMPTY)
        ) {
            final PfShell sshd = start.shell();
            final String clean = "some output";
            new Shell.Plain(
                new SSH(sshd.host(), sshd.port(), sshd.login(), sshd.key())
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
                IOUtils.toString(
                    new Tail(talk.read(), hash).read(),
                    Charset.forName("UTF-8")
                ),
                Matchers.is(String.format("%sê\n", clean))
            );
        }
    }

}
