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
package com.rultor.agents.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.Date;
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
 * @since 1.63
 * @checkstyle ClassDataAbstractionCoupling (200 lines)
 */
public final class ClosePullRequestTest {

    /**
     * ClosePullRequest can close pull request if rebase mode.
     * @throws Exception If error
     * @todo #918:30min This class should only close pull requests for
     *  successful merges, that have not resulted in GitHub itself closing the
     *  PR, do nothing for any other kind of request. Once this is implemented
     *  here, implement this class acting on all pull requests by adding it to
     *  the active Agents.
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
        final Talk talk = ClosePullRequestTest.talk(repo, issue);
        new ClosePullRequest(profile, repo.github()).execute(talk);
        final Issue.Smart smart = new Issue.Smart(issue);
        MatcherAssert.assertThat(
            smart.state(), Matchers.is("closed")
        );
        MatcherAssert.assertThat(
            new Comment.Smart(smart.comments().get(1)).body(),
            Matchers.containsString(
                StringUtils.join(
                    "Rultor closed this pull request for you because your ",
                    ".rultor.yml specified the use of rebasing before ",
                    "merging. GitHub does not mark rebased pull requests as ",
                    "merged, because rebasing entails a change in commit ",
                    "hashes. Nevertheless all your files have been merged ",
                    "exactly as they would have been merged without the ",
                    "rebase option set."
                )
            )
        );
    }

    /**
     * ClosePullRequest can leave issue open if no rebase mode.
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
        final Talk talk = ClosePullRequestTest.talk(repo, issue);
        new ClosePullRequest(profile, repo.github()).execute(talk);
        final Issue.Smart smart = new Issue.Smart(issue);
        MatcherAssert.assertThat(
            smart.state(), Matchers.is("open")
        );
        MatcherAssert.assertThat(
            smart.comments().iterate(new Date(0L)),
            Matchers.is(Matchers.<Comment>emptyIterable())
        );
    }

    /**
     * Creates talk with href, github repository, github issue.
     * @param repo Repository
     * @param issue Issue
     * @return Talk
     * @throws IOException if errors
     */
    private static Talk talk(final Repo repo, final Issue issue)
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
