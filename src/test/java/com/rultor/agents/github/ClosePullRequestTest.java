/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link ClosePullRequest}.
 *
 * @since 1.63
 */
final class ClosePullRequestTest {

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
    void closesPullRequestForRebaseMode() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    "",
                    "<p><entry key='merge'>",
                    "<entry key='rebase'>true</entry>",
                    "</entry></p>"
                ).asString()
            )
        );
        final Talk talk = ClosePullRequestTest.talk(repo, issue);
        new ClosePullRequest(profile, repo.github()).execute(talk);
        final Issue.Smart smart = new Issue.Smart(issue);
        MatcherAssert.assertThat(
            "PR should be closed",
            smart.state(), Matchers.is("closed")
        );
        MatcherAssert.assertThat(
            "Rebase message should be added",
            new Comment.Smart(smart.comments().get(1)).body(),
            Matchers.containsString(
                new Joined(
                    "",
                    "Rultor closed this pull request for you because your ",
                    ".rultor.yml specified the use of rebasing before ",
                    "merging. GitHub does not mark rebased pull requests as ",
                    "merged, because rebasing entails a change in commit ",
                    "hashes. Nevertheless all your files have been merged ",
                    "exactly as they would have been merged without the ",
                    "rebase option set."
                ).asString()
            )
        );
    }

    /**
     * ClosePullRequest can leave issue open if no rebase mode.
     * @throws Exception If error
     */
    @Test
    void leavesPullRequestOpenWhenNoRebaseMode() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Profile profile = new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    "",
                    "<p> <entry key='merge'>",
                    "  <entry key='rebase'>false</entry>",
                    "</entry> </p>"
                ).asString()
            )
        );
        final Talk talk = ClosePullRequestTest.talk(repo, issue);
        new ClosePullRequest(profile, repo.github()).execute(talk);
        final Issue.Smart smart = new Issue.Smart(issue);
        MatcherAssert.assertThat(
            "Issue should be open",
            smart.state(), Matchers.is("open")
        );
        MatcherAssert.assertThat(
            "No comments should be added",
            smart.comments().iterate(new Date(0L)),
            Matchers.is(Matchers.emptyIterable())
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
