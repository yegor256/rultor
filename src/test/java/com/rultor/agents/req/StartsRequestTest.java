/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xembly.Directives;

/**
 * Tests for {@link StartsRequest}.
 *
 * @since 1.3
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
final class StartsRequestTest {
    /**
     * Default head_branch value.
     */
    private static final String HEAD_BRANCH = "main";

    /**
     * StartsRequest can start a request.
     * @throws Exception In case of error.
     */
    @Test
    void startsRequest() throws Exception {
        final Agent agent = new StartsRequest(new Profile.Fixed());
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("request").attr("id", "abcd")
                .add("author").set("yegor256").up()
                .add("type").set("merge").up()
                .add("args")
                .add("arg").attr("name", "hey").set("hello (#dude)!").up()
                .add("arg").attr("name", "heyhey").set("(xyz)")
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Talk should create daemon and script",
            talk.read(),
            XhtmlMatchers.hasXPaths(
                "/talk/daemon[@id='abcd' and script]",
                "/talk/daemon/title",
                "//script[contains(.,\"hey='hello (#dude)!'\")]",
                "//script[contains(.,\"heyhey='(xyz)'\")]",
                "//script[contains(.,'--env=author=yegor256')]",
                "//script[contains(.,'--env=hey=hello (#dude)!')]"
            )
        );
    }

    /**
     * StartsRequest can start a request.
     * @param temp Temporary folder for talk
     * @param jobtemp Temporary folder for job
     * @throws Exception In case of error.
     */
    @Test
    void startsDeployRequest(
        @TempDir final Path temp,
        @TempDir final Path jobtemp
    ) throws Exception {
        final File repo = this.repo(temp);
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
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
                .add("arg").attr("name", "head_branch")
                .set(StartsRequestTest.HEAD_BRANCH)
                .up()
        );
        agent.execute(talk);
        talk.modify(
            new Directives().xpath("/talk/daemon/script").set(
                new Joined(
                    "\n",
                    talk.read().xpath("/talk/daemon/script/text()").get(0),
                    "cd ..; cat entry.sh; cat script.sh"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            "Exec run should contain run commands",
            this.exec(talk, jobtemp),
            Matchers.allOf(
                new Array<Matcher<? super String>>()
                    .with(
                        Matchers.containsString(
                            "image=yegor256/rultor-image\n"
                        )
                    )
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
     * @param temp Temporary folder for talk
     * @param jobtemp Temporary folder for job
     * @throws Exception In case of error.
     */
    @Test
    void startsReleaseRequest(
        @TempDir final Path temp,
        @TempDir final Path jobtemp
    ) throws Exception {
        final File repo = this.repo(temp);
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
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
                .add("arg").attr("name", "head_branch")
                .set(StartsRequestTest.HEAD_BRANCH).up()
                .add("arg").attr("name", "tag").set("1.0-beta").up()
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Release cmds are included in script",
            this.exec(talk, jobtemp),
            Matchers.containsString("echo HEY")
        );
    }

    /**
     * StartsRequest can start a merge request.
     * @param temp Temporary folder for talk
     * @param jobtemp Temporary folder for job
     * @throws Exception In case of error.
     */
    @Test
    void startsMergeRequest(
        @TempDir final Path temp,
        @TempDir final Path jobtemp
    ) throws Exception {
        final File repo = this.repo(temp);
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='merge'><entry key='script'>",
                        "echo \"some('.env');\"</entry></entry></p>"
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
                .add("arg").attr("name", "head_branch")
                .set(StartsRequestTest.HEAD_BRANCH).up()
                .add("arg").attr("name", "fork").set(repo.toString()).up()
                .add("arg").attr("name", "fork_branch").set("frk").up()
                .add("arg").attr("name", "pull_title").set("the \"title").up()
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Merge cmds are included in run",
            this.exec(talk, jobtemp),
            Matchers.containsString("echo \"some('\\''\\'\\'''\\''.env'\\''\\'\\'''\\'');\"")
        );
    }

    /**
     * StartsRequest can not start a merge request.
     * @param temp Temporary folder for talk
     * @param jobtemp Temporary folder for job
     * @throws Exception In case of error.
     */
    @Test
    void startsMergeRequestIfEmpty(
        @TempDir final Path temp,
        @TempDir final Path jobtemp
    ) throws Exception {
        final File repo = this.repo(temp);
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument("<p></p>"),
                "test/test",
                StartsRequestTest.HEAD_BRANCH
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
                .add("arg").attr("name", "head_branch")
                .set(StartsRequestTest.HEAD_BRANCH).up()
                .add("arg").attr("name", "fork").set(repo.toString()).up()
                .add("arg").attr("name", "fork_branch").set("frk").up()
                .add("arg").attr("name", "pull_title").set("the \"title").up()
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Warning message is displayed about missing merge section",
            this.execQuietly(talk, jobtemp),
            Matchers.containsString(
                String.format(
                    "There is no '%s' section in .rultor.yml for branch %s",
                    "merge",
                    StartsRequestTest.HEAD_BRANCH
                )
            )
        );
    }

    /**
     * StartsRequest can run release with dockerfile.
     * @param temp Temporary folder for talk
     * @param jobtemp Temporary folder for job
     * @throws Exception In case of error.
     */
    @Test
    void runsReleaseWithDockerfile(
        @TempDir final Path temp,
        @TempDir final Path jobtemp
    ) throws Exception {
        final File repo = this.repo(temp);
        final File dir = temp.toFile();
        FileUtils.write(
            new File(dir, "Dockerfile"),
            "FROM yegor256/rultor",
            StandardCharsets.UTF_8
        );
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
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
                .add("arg").attr("name", "head_branch")
                .set(StartsRequestTest.HEAD_BRANCH).up()
                .add("arg").attr("name", "tag").set("1.0-beta").up()
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Docker should be run if possible",
            this.execQuietly(talk, jobtemp),
            Matchers.allOf(
                new Array<Matcher<? super String>>()
                    .with(
                        Matchers.containsString(
                            String.format(
                                "head=%s", dir
                            )
                        )
                    )
                    .with(Matchers.containsString("docker_when_possible"))
                    .with(Matchers.containsString("enough to run a new Docker"))
            )
        );
    }

    /**
     * StartsRequest can take decryption instructions into account.
     * @throws Exception In case of error.
     */
    @Test
    void decryptsAssets() throws Exception {
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='decrypt'><entry key='a.txt'>",
                        "a.txt.asc</entry></entry><entry key='deploy'/></p>"
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
            "Decrypt should be in cmds",
            talk.read(),
            XhtmlMatchers.hasXPath(
                "//script[contains(.,'--decrypt')]"
            )
        );
    }

    /**
     * StartsRequest can start a request.
     * @param temp Temporary folder for talk
     * @param jobtemp Temporary folder for job
     * @throws Exception In case of error.
     * @since 1.37
     */
    @Test
    void runsAsRootIfRequested(
        @TempDir final Path temp,
        @TempDir final Path jobtemp
    ) throws Exception {
        final File repo = this.repo(temp);
        final Agent agent = new StartsRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
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
                .add("arg").attr("name", "head_branch")
                .set(StartsRequestTest.HEAD_BRANCH)
                .up()
        );
        agent.execute(talk);
        talk.modify(
            new Directives().xpath("/talk/daemon/script").set(
                new Joined(
                    "\n",
                    talk.read().xpath("/talk/daemon/script/text()").get(0),
                    "cd ..; cat entry.sh"
                ).asString()
            )
        );
        MatcherAssert.assertThat(
            "New user should not be created",
            this.exec(talk, jobtemp),
            Matchers.not(Matchers.containsString("useradd"))
        );
    }

    /**
     * Execute script from daemon.
     * @param talk Talk to use
     * @param wdir Temporary work directory
     * @return Full stdout
     * @throws IOException If fails
     */
    private String exec(final Talk talk, final Path wdir) throws IOException {
        return this.process(talk, wdir).stdout();
    }

    /**
     * Execute script from daemon without throwing exception if fails.
     * @param talk Talk to use
     * @param wdir Temporary work directory
     * @return Full stdout
     * @throws IOException If fails
     */
    private String execQuietly(
        final Talk talk,
        final Path wdir
    ) throws IOException {
        return this.process(talk, wdir).stdoutQuietly();
    }

    /**
     * Create process to execute script from daemon.
     * @param talk Talk to use
     * @param wdir Temporary work directory
     * @return Process
     * @throws IOException If fails
     */
    private VerboseProcess process(
        final Talk talk,
        final Path wdir
    ) throws IOException {
        final String script = new UncheckedText(
            new Joined(
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
            )
        ).asString();
        return new VerboseProcess(
            new ProcessBuilder().command(
                "/bin/bash", "-c", script
            ).directory(wdir.toFile()).redirectErrorStream(true),
            Level.WARNING, Level.WARNING
        );
    }

    /**
     * Create empty Git repo.
     * @param temp Temporary work folder
     * @return Its location
     */
    private File repo(final Path temp) {
        final File repo = temp.toFile();
        final String cmd = new UncheckedText(
            new Joined(
                ";",
                "set -x",
                "set -e",
                "set -o pipefail",
                "git init .",
                "git config user.email test@rultor.com",
                "git config user.name test",
                String.format(
                    "git checkout -b %s",
                    StartsRequestTest.HEAD_BRANCH
                ),
                "echo 'hello, world!' > hello.txt",
                "git add .",
                "git -c commit.gpgsign=false commit -am 'first file'",
                "git checkout -b frk",
                "echo 'good bye!' > hello.txt",
                "git -c commit.gpgsign=false commit -am 'modified file'",
                String.format(
                    "git checkout %s",
                    StartsRequestTest.HEAD_BRANCH
                ),
                "git config receive.denyCurrentBranch ignore"
            )
        ).asString();
        new VerboseProcess(
            new ProcessBuilder().command(
                "/bin/bash",
                "-c",
                cmd
            ).directory(repo)
        ).stdout();
        return repo;
    }

}
