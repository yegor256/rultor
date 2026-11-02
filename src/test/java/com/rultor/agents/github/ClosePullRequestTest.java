/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.time.Instant;
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
     *
     * @throws Exception If error
     */
    @Test
    void closesPullRequestForRebaseMode() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        new ClosePullRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='merge'>",
                        "<entry key='rebase'>true</entry>",
                        "</entry></p>"
                    ).asString()
                )
            ),
            repo.github()
        ).execute(
            ClosePullRequestTest.talk(repo, issue, "merge", true)
        );
        MatcherAssert.assertThat(
            "PR should be closed",
            new Issue.Smart(issue).state(), Matchers.is("closed")
        );
    }

    /**
     * ClosePullRequest can add a rebase message when closing a pull
     * request in rebase mode.
     *
     * @throws Exception If error
     */
    @Test
    void addsRebaseMessageForRebaseMode() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        new ClosePullRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='merge'>",
                        "<entry key='rebase'>true</entry>",
                        "</entry></p>"
                    ).asString()
                )
            ),
            repo.github()
        ).execute(
            ClosePullRequestTest.talk(repo, issue, "merge", true)
        );
        MatcherAssert.assertThat(
            "Rebase message should be added",
            new Comment.Smart(
                new Issue.Smart(issue).comments().get(1)
            ).body(),
            Matchers.containsString(
                "Rultor closed this pull request for you because"
            )
        );
    }

    /**
     * ClosePullRequest can leave issue open if no rebase mode.
     *
     * @throws Exception If error
     */
    @Test
    void leavesPullRequestOpenWhenNoRebaseMode() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        new ClosePullRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p> <entry key='merge'>",
                        "  <entry key='rebase'>false</entry>",
                        "</entry> </p>"
                    ).asString()
                )
            ),
            repo.github()
        ).execute(
            ClosePullRequestTest.talk(repo, issue, "merge", true)
        );
        MatcherAssert.assertThat(
            "Issue should be open",
            new Issue.Smart(issue).state(), Matchers.is("open")
        );
    }

    /**
     * ClosePullRequest does not add comments if no rebase mode.
     *
     * @throws Exception If error
     */
    @Test
    void addsNoCommentsWhenNoRebaseMode() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        new ClosePullRequest(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p> <entry key='merge'>",
                        "  <entry key='rebase'>false</entry>",
                        "</entry> </p>"
                    ).asString()
                )
            ),
            repo.github()
        ).execute(
            ClosePullRequestTest.talk(repo, issue, "merge", true)
        );
        MatcherAssert.assertThat(
            "No comments should be added",
            new Issue.Smart(issue).comments()
                .iterate(Date.from(Instant.EPOCH)),
            Matchers.is(Matchers.emptyIterable())
        );
    }

    @Test
    void leavesPullRequestOpenAfterFailedMerge() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        new ClosePullRequest(
            ClosePullRequestTest.rebase(),
            repo.github()
        ).execute(
            ClosePullRequestTest.talk(repo, issue, "merge", false)
        );
        MatcherAssert.assertThat(
            "Pull request must not be closed after a failed merge",
            new Issue.Smart(issue).state(), Matchers.is("open")
        );
    }

    @Test
    void leavesPullRequestOpenAfterDeploy() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        new ClosePullRequest(
            ClosePullRequestTest.rebase(),
            repo.github()
        ).execute(
            ClosePullRequestTest.talk(repo, issue, "deploy", true)
        );
        MatcherAssert.assertThat(
            "Pull request must not be closed after a deploy",
            new Issue.Smart(issue).state(), Matchers.is("open")
        );
    }

    @Test
    void addsNoCommentToAlreadyClosedPullRequest() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        new Issue.Smart(issue).close();
        new ClosePullRequest(
            ClosePullRequestTest.rebase(),
            repo.github()
        ).execute(
            ClosePullRequestTest.talk(repo, issue, "merge", true)
        );
        MatcherAssert.assertThat(
            "Closed pull request must not receive a comment",
            new Issue.Smart(issue).comments()
                .iterate(Date.from(Instant.EPOCH)),
            Matchers.is(Matchers.emptyIterable())
        );
    }

    private static Profile rebase() throws Exception {
        return new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    "",
                    "<p><entry key='merge'>",
                    "<entry key='rebase'>true</entry>",
                    "</entry></p>"
                ).asString()
            )
        );
    }

    private static Talk talk(final Repo repo, final Issue issue,
        final String type, final boolean success) throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("wire")
                .add("href").set("http://test").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue.number()))
                .up().up()
                .add("request").attr("id", "a1b2c3")
                .add("author").set("yegor256").up()
                .add("success").set(Boolean.toString(success)).up()
                .add("type").set(type).up()
                .add("args")
        );
        return talk;
    }
}
