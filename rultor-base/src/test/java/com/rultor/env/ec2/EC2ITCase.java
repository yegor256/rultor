/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.env.ec2;

import com.rultor.env.Environments;
import com.rultor.shell.Shell;
import com.rultor.shell.Shells;
import com.rultor.shell.ssh.PrivateKey;
import com.rultor.shell.ssh.SSHServers;
import java.io.ByteArrayOutputStream;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link EC}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class EC2ITCase {

    /**
     * EC2 can make environments.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesInstanceAndConnectsToIt() throws Exception {
        final String key = System.getProperty("failsafe.ec2.key");
        Assume.assumeNotNull(key);
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        final Environments envs = new EC2(
            "t1.micro",
            "ami-82fa58eb",
            "rultor-test",
            "rultor-test",
            key,
            System.getProperty("failsafe.ec2.secret")
        );
        final Shells shells = new SSHServers(
            envs,
            "ubuntu",
            new PrivateKey(System.getProperty("failsafe.ec2.priv"))
        );
        final Shell shell = shells.acquire();
        int code;
        try {
            code = shell.exec(
                "#!/bin/bash\nfor i in '1 2 3 4'\ndo\necho $i\ndone",
                IOUtils.toInputStream(""),
                stdout,
                stderr
            );
        } finally {
            shell.close();
        }
        MatcherAssert.assertThat(code, Matchers.equalTo(0));
        MatcherAssert.assertThat(
            stdout.toString(CharEncoding.UTF_8),
            Matchers.equalTo("1 2 3 4\n")
        );
        MatcherAssert.assertThat(
            stderr.toString(CharEncoding.UTF_8),
            Matchers.equalTo("")
        );
    }

}
