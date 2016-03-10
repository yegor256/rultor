/**
 * Copyright (c) 2009-2016, rultor.com
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

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Tests for {@link ClosePullRequest}.
 *
 * @author Viktor Kuchyn (kuchin.victor@gmail.com)
 * @version $Id$
 * @since 2.0
 * @checkstyle ClassDataAbstractionCoupling (200 lines)
 */
public final class ClosePullRequestTest {

    /**
     * ClosePullRequest closes pull request if rebase mode.
     * @throws Exception If error
     */
    @Test
    public void closesPullRequestForRebaseMode() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                StringUtils.join(
                    "<p><entry key='merge'>",
                    "<entry key='rebase'>true</entry>",
                    "</entry></p>"
                )
            )
        );
        final Agent agent = new ClosePullRequest(profile, repo.github());
        final Talk talk = ClosePullRequestTest.createTalk(repo, issue);
        agent.execute(talk);
        MatcherAssert.assertThat(
            new Issue.Smart(issue).state(), Matchers.is("closed")
        );
    }

    /**
     * ClosePullRequest leaves issue open if no rebase mode.
     * @throws Exception If error
     */
    @Test
    public void leavesPullRequestOpenWhenNoRebaseMode() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                StringUtils.join(
                    "<p> <entry key='merge'>",
                    "  <entry key='rebase'>false</entry>",
                    "</entry> </p>"
                )
            )
        );
        final Agent agent = new ClosePullRequest(profile, repo.github());
        final Talk talk = ClosePullRequestTest.createTalk(repo, issue);
        agent.execute(talk);
        MatcherAssert.assertThat(
            new Issue.Smart(issue).state(), Matchers.is("open")
        );
    }

    /**
     * ClosePullRequest can comment issue with explanation.
     * @throws Exception If error
     */
    @Test
    public void commentsToIssueWithExplanation() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                StringUtils.join(
                    "<p><entry key='merge' >",
                    "<entry key='rebase' >true</entry>",
                    " </entry> </p>"
                )
            )
        );
        final Agent agent = new ClosePullRequest(profile, repo.github());
        final Talk talk = ClosePullRequestTest.createTalk(repo, issue);
        agent.execute(talk);
        final Comment.Smart comment = new Comment.Smart(
            new Issue.Smart(issue).comments().get(1)
        );
        MatcherAssert.assertThat(
            comment.body(), Matchers.containsString(
                "Closed manually because of rebase mode"
            )
        );
    }

    /**
     * Creates talk with href, github repository, github issue.
     * @param repo Repository
     * @param issue Issue
     * @return Talk
     * @throws IOException if errors
     */
    private static Talk createTalk(final Repo repo, final Issue issue)
        throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("wire")
                .add("href").set("http://test").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue.number()))
        );
        return talk;
    }

}
