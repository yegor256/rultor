/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Comments;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Issue;
import com.jcabi.github.Pull;
import com.jcabi.github.PullRef;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.agents.github.Req;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.ResourceBundle;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnRebase}.
 *
 * @since 2.1
 */
final class QnRebaseTest {

    /**
     * The default command to the rultor with a request to rebase a branch.
     */
    private static final String COMMAND = "@rultor, rebase, please";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * QnRebase can build a request.
     *
     * @throws Exception In case of error
     */
    @Test
    void buildsRequest() throws Exception {
        final Pull pull = QnRebaseTest.pull();
        MatcherAssert.assertThat(
            "Rebase request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnRebase().understand(
                        new Comment.Smart(
                            pull.repo().issues().get(pull.number())
                                .comments().post(QnRebaseTest.COMMAND)
                        ),
                        new URI("#")
                    ).dirs()
                )
            ).xml(),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("/request/type[text()='rebase']"),
                XhtmlMatchers.hasXPath(
                    "/request/args/arg[@name='fork_branch' and text()='head']"
                ),
                XhtmlMatchers.hasXPath(
                    "/request/args/arg[@name='head_branch' and text()='base']"
                )
            )
        );
    }

    /**
     * QnRebase posts the initiator command as the first comment.
     *
     * @throws Exception In case of error
     */
    @Test
    void postsInitiatorCommentWhenRebaseRequested() throws Exception {
        final Pull pull = QnRebaseTest.pull();
        final Comments comments =
            pull.repo().issues().get(pull.number()).comments();
        new Directives().add("request").append(
            new QnRebase().understand(
                new Comment.Smart(comments.post(QnRebaseTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Rebase comment should be initiator",
            new Comment.Smart(comments.get(1)).body(),
            Matchers.is(QnRebaseTest.COMMAND)
        );
    }

    /**
     * QnRebase posts a comment about starting the rebase.
     *
     * @throws Exception In case of error
     */
    @Test
    void postsStartCommentWhenRebaseRequested() throws Exception {
        final Pull pull = QnRebaseTest.pull();
        final Comments comments =
            pull.repo().issues().get(pull.number()).comments();
        new Directives().add("request").append(
            new QnRebase().understand(
                new Comment.Smart(comments.post(QnRebaseTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Comment about starting rebase should be posted",
            new Comment.Smart(comments.get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnRebaseTest.PHRASES.getString("QnRebase.start"),
                    "#"
                )
            )
        );
    }

    /**
     * QnRebase can not build a request for a closed pull request.
     *
     * @throws Exception In case of error
     */
    @Test
    void returnsNothingWhenPullIsClosed() throws Exception {
        final Pull pull = QnRebaseTest.pull();
        final Issue issue = pull.repo().issues().get(pull.number());
        new Issue.Smart(issue).close();
        MatcherAssert.assertThat(
            "Nothing should be requested for a closed pull request",
            new QnRebase().understand(
                new Comment.Smart(
                    issue.comments().post(QnRebaseTest.COMMAND)
                ),
                new URI("#")
            ),
            Matchers.equalTo(Req.EMPTY)
        );
    }

    /**
     * QnRebase posts a comment when the pull request is closed.
     *
     * @throws Exception In case of error
     */
    @Test
    void postsClosedCommentWhenPullIsClosed() throws Exception {
        final Pull pull = QnRebaseTest.pull();
        final Issue issue = pull.repo().issues().get(pull.number());
        new Issue.Smart(issue).close();
        final Comments comments = issue.comments();
        new QnRebase().understand(
            new Comment.Smart(comments.post(QnRebaseTest.COMMAND)),
            new URI("#")
        );
        MatcherAssert.assertThat(
            "Comment should be posted about the closed pull request",
            new Comment.Smart(comments.get(2)).body(),
            Matchers.containsString(
                QnRebaseTest.PHRASES.getString("QnRebase.already-closed")
            )
        );
    }

    /**
     * QnRebase refuses to rebase a branch from an external fork, since
     * it does not have push permissions there.
     *
     * @throws Exception In case of error
     */
    @Test
    void returnsNothingWhenHeadIsFromExternalFork() throws Exception {
        MatcherAssert.assertThat(
            "Nothing should be requested for an external fork",
            new QnRebase().understand(
                QnRebaseTest.externalForkComment(),
                new URI("#")
            ),
            Matchers.equalTo(Req.DONE)
        );
    }

    /**
     * QnRebase posts a comment when the head branch comes from
     * an external fork.
     *
     * @throws Exception In case of error
     */
    @Test
    void postsForkCommentWhenHeadIsFromExternalFork() throws Exception {
        final Comment.Smart comment = QnRebaseTest.externalForkComment();
        new QnRebase().understand(comment, new URI("#"));
        final ArgumentCaptor<String> message =
            ArgumentCaptor.forClass(String.class);
        Mockito.verify(
            comment.issue().comments(),
            Mockito.times(2)
        ).post(message.capture());
        MatcherAssert.assertThat(
            "Comment should be posted about the external fork",
            message.getAllValues().get(1),
            Matchers.containsString(
                QnRebaseTest.PHRASES.getString("QnRebase.fork-not-supported")
            )
        );
    }

    private static Pull pull() throws IOException {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        return repo.pulls().create("", "head", "base");
    }

    private static Comment.Smart externalForkComment() throws IOException {
        final Comment.Smart comment =
            Mockito.mock(Comment.Smart.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(comment.body()).thenReturn(QnRebaseTest.COMMAND);
        Mockito.when(comment.author().login()).thenReturn("alice");
        Mockito.when(comment.issue().json())
            .thenReturn(QnRebaseTest.issueJson());
        Mockito.when(comment.issue().number()).thenReturn(1);
        Mockito.when(
            comment.issue().comments().iterate(Mockito.any(Date.class))
        ).thenReturn(Collections.emptyList());
        Mockito.when(
            comment.issue().repo().github().users().self().login()
        ).thenReturn("rultor");
        Mockito.when(
            comment.issue().repo().github().users().get("alice").login()
        ).thenReturn("alice");
        final Pull pull = comment.issue().repo().pulls().get(1);
        final PullRef head = pull.head();
        Mockito.when(head.json()).thenReturn(QnRebaseTest.headJson());
        Mockito.when(head.repo().coordinates())
            .thenReturn(new Coordinates.Simple("alice/fork"));
        Mockito.when(pull.base().repo().coordinates())
            .thenReturn(new Coordinates.Simple("alice/base"));
        return comment;
    }

    private static JsonObject issueJson() {
        return Json.createObjectBuilder()
            .add("state", Issue.OPEN_STATE)
            .add("user", QnRebaseTest.userAlice())
            .add("pull_request", QnRebaseTest.pullRequestJson())
            .build();
    }

    private static JsonObject headJson() {
        return Json.createObjectBuilder()
            .add("repo", QnRebaseTest.repoJson())
            .build();
    }

    private static JsonObject userAlice() {
        return Json.createObjectBuilder().add("login", "alice").build();
    }

    private static JsonObject pullRequestJson() {
        return Json.createObjectBuilder()
            .add("html_url", "https://github.com/alice/fork/pull/1")
            .add("number", 1)
            .build();
    }

    private static JsonObject repoJson() {
        return Json.createObjectBuilder()
            .add("name", "alice/fork")
            .build();
    }
}
