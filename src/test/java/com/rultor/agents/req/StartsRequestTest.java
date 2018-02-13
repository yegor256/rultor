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

import com.jcabi.immutable.Array;
import com.jcabi.log.VerboseProcess;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.cactoos.text.JoinedText;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xembly.Directives;

/**
 * Tests for {@link StartsRequest}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class StartsRequestTest {

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * StartsRequest can start a request.
     * @throws Exception In case of error.
     */
    @Test
    public void startsRequest() throws Exception {
        final Agent agent = new StartsRequest(new Profile.Fixed());
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "abcd")
                .add("author").set("yegor256").up()
                .add("type").set("merge").up()
                .add("args")
                .add("arg").attr("name", "hey").set("hello dude!")
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPaths(
                "/talk/daemon[@id='abcd' and script]",
                "/talk/daemon/title",
                "//script[contains(.,\"hey='hello dude!'\")]",
                "//script[contains(.,'--env=author=yegor256')]",
                "//script[contains(.,'--env=hey=hello dude!')]"
            )
        );
    }

    /**
     * StartsRequest can start a request.
     * @throws Exception In case of error.
     */
    @Test
    public void startsDeployRequest() throws Exception {
        final File repo = this.repo();
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                        " ",
                        "<p><entry key='deploy'>",
                        "<entry key='script'>echo HEY</entry>",
                        "<entry key='env'>",
                        "<entry key='MAVEN_OPTS'>-Xmx2g -Xms1g</entry>",
                        "</entry></entry></p>"
                    ).asString()
                )
            )
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "abcd")
                .add("author").set("yegor256").up()
                .add("type").set("deploy").up()
                .add("args")
                .add("arg").attr("name", "head").set(repo.toString()).up()
                .add("arg").attr("name", "head_branch").set("master").up()
        );
        agent.execute(talk);
        talk.modify(
            new Directives().xpath("/talk/daemon/script").set(
                new JoinedText(
                    "\n",
                    talk.read().xpath("/talk/daemon/script/text()").get(0),
                    "cd ..; cat entry.sh; cat script.sh"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            this.exec(talk),
            Matchers.allOf(
                new Array<Matcher<? super String>>()
                    .with(Matchers.containsString("image=yegor256/rultor\n"))
                    .with(
                        Matchers.containsString(
                            String.format(
                                "container=%s\n",
                                Talk.TEST_NAME
                            )
                        )
                    )
                    .with(Matchers.containsString("Cloning into 'repo'...\n"))
                    .with(Matchers.containsString("docker_when_possible\n"))
                    .with(Matchers.containsString("load average is "))
                    .with(Matchers.containsString("low enough to run a"))
                    .with(Matchers.containsString("DOCKER-2: -t"))
                    .with(
                        Matchers.containsString(
                            "DOCKER-6: --env=MAVEN_OPTS=-Xmx2g -Xms1g"
                        )
                    )
                    .with(Matchers.containsString("useradd"))
            )
        );
    }

    /**
     * StartsRequest can start a release request.
     * @throws Exception In case of error.
     */
    @Test
    public void startsReleaseRequest() throws Exception {
        final File repo = this.repo();
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                        "",
                        "<p><entry key='release'><entry key='script'>",
                        "echo HEY</entry></entry>",
                        "<entry key='docker'><entry key='image'>a/b</entry>",
                        "</entry></p>"
                    ).asString()
                )
            )
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "a8b9c0")
                .add("author").set("yegor256").up()
                .add("type").set("release").up()
                .add("args")
                .add("arg").attr("name", "head").set(repo.toString()).up()
                .add("arg").attr("name", "head_branch").set("master").up()
                .add("arg").attr("name", "tag").set("1.0-beta").up()
        );
        agent.execute(talk);
        this.exec(talk);
    }

    /**
     * StartsRequest can start a merge request.
     * @throws Exception In case of error.
     */
    @Test
    public void startsMergeRequest() throws Exception {
        final File repo = this.repo();
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                        "",
                        "<p><entry key='merge'><entry key='script'>",
                        "echo HEY</entry></entry></p>"
                    ).asString()
                )
            )
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "a1b2c3")
                .add("author").set("yegor256").up()
                .add("type").set("merge").up()
                .add("args")
                .add("arg").attr("name", "head").set(repo.toString()).up()
                .add("arg").attr("name", "head_branch").set("master").up()
                .add("arg").attr("name", "fork").set(repo.toString()).up()
                .add("arg").attr("name", "fork_branch").set("frk").up()
                .add("arg").attr("name", "pull_title").set("the \"title").up()
        );
        agent.execute(talk);
        this.exec(talk);
    }

    /**
     * StartsRequest can run release with dockerfile (the test is disabled,
     * because it doesn't work on Mac, see #702).
     * @throws Exception In case of error.
     */
    @Test
    @Ignore
    public void runsReleaseWithDockerfile() throws Exception {
        final File repo = this.repo();
        final File dir = this.temp.newFolder();
        FileUtils.write(new File(dir, "Dockerfile"), "FROM yegor256/rultor");
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                       "",
                        "<p><entry key='release'><entry key='script'>",
                        "echo HEY</entry></entry><entry key='docker'>",
                        String.format("<entry key='directory'>%s</entry>", dir),
                        "</entry></p>"
                    ).asString()
                )
            )
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "a8b9c0")
                .add("author").set("yegor256").up()
                .add("type").set("release").up()
                .add("args")
                .add("arg").attr("name", "head").set(repo.toString()).up()
                .add("arg").attr("name", "head_branch").set("master").up()
                .add("arg").attr("name", "tag").set("1.0-beta").up()
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            this.exec(talk),
            Matchers.allOf(
                new Array<Matcher<? super String>>()
                    .with(
                        Matchers.containsString(
                            String.format(
                                "docker build %s -t yegor256/rultor-", dir
                        )
                    )
                )
                    .with(Matchers.containsString("docker run"))
                    .with(
                        Matchers.containsString(
                                "docker rmi yegor256/rultor-"
                        )
                    )
                    .with(Matchers.containsString("low enough to run a"))
            )
        );
    }

    /**
     * StartsRequest can take decryption instructions into account.
     * @throws Exception In case of error.
     */
    @Test
    public void decryptsAssets() throws Exception {
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                        "",
                        "<p><entry key='decrypt'><entry key='a.txt'>",
                        "a.txt.asc</entry></entry></p>"
                    ).asString()
                )
            )
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "ff89")
                .add("author").set("yegor256").up()
                .add("type").set("deploy").up().add("args")
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath(
                "//script[contains(.,'--decrypt')]"
            )
        );
    }

    /**
     * StartsRequest can start a request.
     * @throws Exception In case of error.
     * @since 1.37
     */
    @Test
    public void runsAsRootIfRequested() throws Exception {
        final File repo = this.repo();
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                        "",
                        "<p><entry key='docker'>",
                        "<entry key='as_root'>true</entry></entry>",
                        "<entry key='deploy'>",
                        "<entry key='script'>echo BOOM</entry></entry></p>"
                    ).asString()
                )
            )
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "abcd")
                .add("author").set("yegor256").up()
                .add("type").set("deploy").up()
                .add("args")
                .add("arg").attr("name", "head").set(repo.toString()).up()
                .add("arg").attr("name", "head_branch").set("master").up()
        );
        agent.execute(talk);
        talk.modify(
            new Directives().xpath("/talk/daemon/script").set(
                new JoinedText(
                    "\n",
                    talk.read().xpath("/talk/daemon/script/text()").get(0),
                    "cd ..; cat entry.sh"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            this.exec(talk),
            Matchers.not(Matchers.containsString("useradd"))
        );
    }

    /**
     * Execute script from daemon.
     * @param talk Talk to use
     * @return Full stdout
     * @throws java.io.IOException If fails
     */
    private String exec(final Talk talk) throws IOException {
        final String script = new JoinedText(
            "\n",
            "set -x",
            "set -e",
            "set -o pipefail",
            "function docker {",
            "  for (( i=1; i<=$#; i++ )); do",
            "    echo \"DOCKER-$i: ${!i}\"",
            "  done",
            "}",
            "function sudo {",
            "  for (( i=1; i<=$#; i++ )); do ",
            "    echo \"SUDO-$i: ${!i}\"",
            "  done",
            "} ",
            talk.read().xpath("//script/text()").get(0)
        ).asString();
        return new VerboseProcess(
            new ProcessBuilder().command(
                "/bin/bash", "-c", script
            ).directory(this.temp.newFolder()).redirectErrorStream(true),
            Level.WARNING, Level.WARNING
        ).stdout();
    }

    /**
     * Create empty Git repo.
     * @return Its location
     * @throws IOException If fails
     */
    private File repo() throws IOException {
        final File repo = this.temp.newFolder();
        new VerboseProcess(
            new ProcessBuilder().command(
                "/bin/bash",
                "-c",
                new JoinedText(
                    ";",
                    "set -x",
                    "set -e",
                    "set -o pipefail",
                    "git init .",
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
            ).directory(repo)
        ).stdout();
        return repo;
    }

}
