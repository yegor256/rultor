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
package com.rultor.agents.github;

import com.jcabi.github.Check;
import com.jcabi.github.Comment;
import com.jcabi.github.Comments;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Issue;
import com.jcabi.github.Pull;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkChecks;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.agents.github.qtn.QnDeploy;
import com.rultor.agents.github.qtn.QnFirstOf;
import com.rultor.agents.github.qtn.QnHello;
import com.rultor.agents.github.qtn.QnIamLost;
import com.rultor.agents.github.qtn.QnIfContains;
import com.rultor.agents.github.qtn.QnMerge;
import com.rultor.agents.github.qtn.QnWithAuthor;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link Understands}.
 *
 * @since 1.3
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class UnderstandsTest {

    /**
     * Understands can understand a message.
     * @throws Exception In case of error.
     */
    @Test
    void understandsMessage() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("@jeff hello");
        issue.comments().post("@jeff deploy");
        final Agent agent = new Understands(
            repo.github(),
            new QnWithAuthor(
                new QnFirstOf(
                    Arrays.asList(
                        new QnIfContains("hello", new QnHello()),
                        new QnIfContains("deploy", new QnDeploy())
                    )
                )
            )
        );
        final Talk talk = UnderstandsTest.talk(issue);
        agent.execute(talk);
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPaths(
                "/talk/wire[github-seen='2']",
                "/talk/request[@id='2']",
                "/talk/request[type='deploy' and args]",
                "/talk/request/args/arg[@name='head_branch' and .='master']"
            )
        );
    }

    /**
     * Understands can ignore LATER req.
     * @throws Exception In case of error.
     */
    @Test
    void ignoresLaterReq() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("@jeff hey you");
        final Agent agent = new Understands(
            repo.github(),
            new Question() {
                @Override
                public Req understand(final Comment.Smart cmt, final URI home) {
                    return Req.LATER;
                }
            }
        );
        final Talk talk = UnderstandsTest.talk(issue);
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPaths("/talk[not(request)]")
        );
    }

    /**
     * Understands can understand a body of an issue.
     * @throws Exception In case of error.
     */
    @Test
    void understandsIssueBody() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("test", "@test hello");
        final Agent agent = new Understands(
            repo.github(),
            new QnIfContains("hello", new QnHello())
        );
        final Talk talk = UnderstandsTest.talk(issue);
        agent.execute(talk);
        MatcherAssert.assertThat(
            issue.comments().iterate(new Date(0L)),
            Matchers.iterableWithSize(1)
        );
    }

    /**
     * The test that verifies that only one message is print if the
     * pull request has a failed check.
     * Test for issue #1657
     * @throws Exception In case of error.
     */
    @Test
    void understandsMergeMessageWithFailedCheck() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        ((MkChecks) pull.checks()).create(
            Check.Status.COMPLETED,
            Check.Conclusion.FAILURE
        );
        new Understands(
            repo.github(),
            new QnFirstOf(
                new QnMerge(),
                new QnIamLost()
            )
        ).execute(UnderstandsTest.talk(pull));
        final Comments comments = repo.issues().get(1).comments();
        MatcherAssert.assertThat(
            comments.iterate(new Date(0)),
            Matchers.iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(comments.get(1)).body(),
            Matchers.containsString("Can't merge")
        );
    }

    /**
     * Make talk from issue.
     * @param issue The issue
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(final Issue issue) throws IOException {
        return UnderstandsTest.talk(issue.repo().coordinates(), issue.number());
    }

    /**
     * Make talk from issue.
     * @param pull The issue
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(final Pull pull) throws IOException {
        return UnderstandsTest.talk(
            pull.repo().coordinates(),
            pull.number()
        );
    }

    /**
     * Make talk from coordinates and number.
     * @param coordinates Repo Coordinates
     * @param number Issue Number
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(
        final Coordinates coordinates,
        final int number
    ) throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .attr("later", "true")
                .add("wire")
                .add("href").set("http://test2").up()
                .add("github-repo").set(coordinates.toString())
                .up()
                .add("github-issue").set(Integer.toString(number)).up()
        );
        return talk;
    }

}
