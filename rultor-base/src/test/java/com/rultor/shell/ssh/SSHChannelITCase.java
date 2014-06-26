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
package com.rultor.shell.ssh;

import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.util.logging.Level;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Integration case for {@link SSHChannel}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class SSHChannelITCase {

    /**
     * Host name of the server to check (change it manually for every run).
     */
    private static final String HOST =
        "ec2-54-234-247-79.compute-1.amazonaws.com";

    /**
     * Private SSH key.
     */
    private static final String KEY = System.getProperty("failsafe.ec2.priv");

    /**
     * EC2 can execute bash scripts remotely (to enable the test you should
     * get a real IP address of a running EC2 environment, created with
     * rultor-test key pair and in rultor-test security group).
     * @throws Exception If some problem inside
     */
    @Test
    @org.junit.Ignore
    public void makesInstanceAndConnectsToIt() throws Exception {
        final SSHChannel channel = new SSHChannel(
            InetAddress.getByName(SSHChannelITCase.HOST),
            "ubuntu",
            new PrivateKey(SSHChannelITCase.KEY)
        );
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        final int code = channel.exec(
            "#!/bin/bash\nfor i in '1 2 3 4'\ndo\necho $i\ndone",
            IOUtils.toInputStream(""),
            new TeeOutputStream(stdout, Logger.stream(Level.INFO, this)),
            new TeeOutputStream(stderr, Logger.stream(Level.SEVERE, this))
        );
        MatcherAssert.assertThat(code, Matchers.equalTo(0));
        MatcherAssert.assertThat(
            stderr.toString(CharEncoding.UTF_8),
            Matchers.equalTo("")
        );
        MatcherAssert.assertThat(
            stdout.toString(CharEncoding.UTF_8),
            Matchers.equalTo("1 2 3 4\n")
        );
    }

}
