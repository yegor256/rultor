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
package com.rultor.agents.req;

import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import org.cactoos.text.JoinedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Integration tests for ${@link StartsRequest}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.24.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class StartsRequestITCase {

    /**
     * Docker server address.
     */
    private static final String HOST =
        System.getProperty("failsafe.docker.host");

    /**
     * Docker server username.
     */
    private static final String LOGIN =
        System.getProperty("failsafe.docker.login");

    /**
     * Docker server SSH key.
     */
    private static final String KEY =
        System.getProperty("failsafe.docker.key");

    /**
     * StartsRequest can compose an executable DEPLOY request.
     * @throws Exception In case of error.
     */
    @Test
    public void composesCorrectDeployRequest() throws Exception {
        Assume.assumeNotNull(StartsRequestITCase.HOST);
        final Shell shell = new Shell.Verbose(
            new SSH(
                StartsRequestITCase.HOST, 22,
                StartsRequestITCase.LOGIN, StartsRequestITCase.KEY
            )
        );
        final String repo = String.format("/tmp/%d.git", System.nanoTime());
        new Shell.Plain(new Shell.Safe(shell)).exec(
            new JoinedText(
                ";",
                "cd /tmp",
                String.format("git init %s", repo),
                String.format("cd %s", repo),
                "git config user.email test@rultor.com",
                "git config user.name test",
                "echo 'hello, world!' > hello.txt",
                "git add .",
                "git commit -am 'first file'",
                "git checkout -b frk",
                "echo 'good bye!' > hello.txt",
                "git commit -am 'modified file'",
                "git checkout master",
                "git config receive.denyCurrentBranch ignore"
            ).asString()
        );
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                        "\n",
                        "<p><entry key='deploy'><entry key='script'>",
                        "echo 'Hello, world!'",
                        "echo 'I am' $(id -u -n)",
                        "</entry></entry></p>"
                    ).asString()
                )
            )
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "abcd")
                .add("type").set("deploy").up()
                .add("author").set("yegor256").up()
                .add("args")
                .add("arg").attr("name", "head").set(repo).up()
                .add("arg").attr("name", "head_branch").set("master").up()
        );
        agent.execute(talk);
        final String dir = String.format("/tmp/test-%d", System.nanoTime());
        final String stdout = new Shell.Plain(shell).exec(
            new JoinedText(
                "\n",
                String.format("mkdir %s", dir),
                String.format("cd %s", dir),
                talk.read().xpath("//script/text()").get(0)
            ).asString()
        );
        new Shell.Plain(new Shell.Safe(shell)).exec(
            new JoinedText(
                "\n",
                String.format("rm -rf %s", repo),
                String.format("sudo rm -rf %s", dir)
            ).asString()
        );
        MatcherAssert.assertThat(
            stdout,
            Matchers.allOf(
                Matchers.containsString("Hello, world!"),
                Matchers.containsString("I am r")
            )
        );
    }

}
