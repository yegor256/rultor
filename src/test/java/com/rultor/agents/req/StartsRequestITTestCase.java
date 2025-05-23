/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Integration tests for ${@link StartsRequest}.
 *
 * @since 1.24.1
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class StartsRequestITTestCase {
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
     * @checkstyle NonStaticMethodCheck (100 lines)
     */
    @Test
    void composesCorrectDeployRequest() throws Exception {
        Assumptions.assumeTrue(StartsRequestITTestCase.HOST != null);
        Assumptions.assumeFalse(StartsRequestITTestCase.HOST.isEmpty());
        final Shell shell = new Shell.Verbose(
            new Ssh(
                StartsRequestITTestCase.HOST, 22,
                StartsRequestITTestCase.LOGIN, StartsRequestITTestCase.KEY
            )
        );
        final String repo = String.format("/tmp/%d.git", System.nanoTime());
        new Shell.Plain(new Shell.Safe(shell)).exec(
            new Joined(
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
                    new Joined(
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
            new Joined(
                "\n",
                String.format("mkdir %s", dir),
                String.format("cd %s", dir),
                talk.read().xpath("//script/text()").get(0)
            ).asString()
        );
        new Shell.Plain(new Shell.Safe(shell)).exec(
            new Joined(
                "\n",
                String.format("rm -rf %s", repo),
                String.format("sudo rm -rf %s", dir)
            ).asString()
        );
        MatcherAssert.assertThat(
            "stdout should contain executed commands",
            stdout,
            Matchers.allOf(
                Matchers.containsString("Hello, world!"),
                Matchers.containsString("I am r")
            )
        );
    }

}
