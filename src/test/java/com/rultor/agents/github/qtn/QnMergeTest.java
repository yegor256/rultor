/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Check;
import com.jcabi.github.Comment;
import com.jcabi.github.Comments;
import com.jcabi.github.Pull;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkChecks;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnMerge}.
 * @since 1.6
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class QnMergeTest {

    /**
     * The default command to the rultor with a request to merge changes.
     */
    private static final String COMMAND = "@rultor, merge, please";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * QnMerge creates a merge request with the expected XML structure.
     * @throws Exception In case of error
     */
    @Test
    void buildsRequest() throws Exception {
        final Pull pull = QnMergeTest.pull();
        MatcherAssert.assertThat(
            "Merge request should be created",
            new Xembler(QnMergeTest.mergeRequest(pull)).xml(),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("/request/type[text()='merge']"),
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
     * QnMerge keeps the initiator command as the first comment.
     * @throws Exception In case of error
     */
    @Test
    void preservesInitiatorComment() throws Exception {
        final Pull pull = QnMergeTest.pull();
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
    }

    /**
     * QnMerge posts the merge start comment.
     * @throws Exception In case of error
     */
    @Test
    void postsMergeStartComment() throws Exception {
        final Pull pull = QnMergeTest.pull();
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Comment about staring merge should be posted",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnMergeTest.PHRASES.getString("QnMerge.start"),
                    "#"
                )
            )
        );
    }

    /**
     * QnMerge keeps the initiator comment when CI checks fail.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void preservesInitiatorWhenCiChecksFailed()
        throws IOException, URISyntaxException {
        final Pull pull = QnMergeTest.pull();
        QnMergeTest.statusesOf(pull).create(
            Check.Status.IN_PROGRESS, Check.Conclusion.SUCCESS
        );
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
    }

    /**
     * QnMerge can not build a request because some GitHub checks
     *  were failed.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void stopsBecauseCiChecksFailed() throws IOException, URISyntaxException {
        final Pull pull = QnMergeTest.pull();
        QnMergeTest.statusesOf(pull).create(
            Check.Status.IN_PROGRESS, Check.Conclusion.SUCCESS
        );
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Merge should be stopped if checks are not successful",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(2)).body(),
            Matchers.containsString(
                QnMergeTest.PHRASES.getString("QnMerge.checks-are-failed")
            )
        );
    }

    /**
     * QnMerge keeps the initiator comment when CI checks pass.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void preservesInitiatorWhenCiChecksSuccessful()
        throws IOException, URISyntaxException {
        final Pull pull = QnMergeTest.pull();
        QnMergeTest.statusesOf(pull).create(
            Check.Status.COMPLETED, Check.Conclusion.SUCCESS
        );
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
    }

    /**
     * QnMerge can build a request because GitHub checks finished successfully.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void continuesBecauseCiChecksSuccessful()
        throws IOException, URISyntaxException {
        final Pull pull = QnMergeTest.pull();
        QnMergeTest.statusesOf(pull).create(
            Check.Status.COMPLETED, Check.Conclusion.SUCCESS
        );
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Merge start info comment should be posted",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnMergeTest.PHRASES.getString("QnMerge.start"),
                    "#"
                )
            )
        );
    }

    /**
     * QnMerge can build a request when some CI checks are skipped.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void continuesBecauseSomeChecksAreSkipped()
        throws IOException, URISyntaxException {
        final Pull pull = QnMergeTest.pull();
        final MkChecks statuses = QnMergeTest.statusesOf(pull);
        statuses.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        statuses.create(Check.Status.COMPLETED, Check.Conclusion.SKIPPED);
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Merge should proceed when some checks are skipped",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnMergeTest.PHRASES.getString("QnMerge.start"),
                    "#"
                )
            )
        );
    }

    /**
     * QnMerge can not build a request because .rultor file is changed.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     * @todo #1459 Enable this test after com.jcabi.github.mock.MkPull
     *  changed to allow to work with the files in Pull according to
     *  https://github.com/jcabi/jcabi-github/issues/1720
     */
    @Test
    @Disabled
    void stopsBecauseSystemFilesAffected()
        throws IOException, URISyntaxException {
        final Pull pull = QnMergeTest.pull();
        QnMergeTest.statusesOf(pull).create(
            Check.Status.COMPLETED, Check.Conclusion.SUCCESS
        );
        final List<JsonObject> files = new LinkedList<>();
        files.add(
            Json.createObjectBuilder()
                .add("sha", "ef36558cbd")
                .add("filename", "README.md")
                .add("status", "modified")
                .build()
        );
        files.add(
            Json.createObjectBuilder()
                .add("sha", "ef3857cad")
                .add("filename", ".rultor.yml")
                .add("status", "modified")
                .build()
        );
        QnMergeTest.mergeRequest(pull);
        MatcherAssert.assertThat(
            "Comment should be posted about affected system file",
            new Comment.Smart(QnMergeTest.commentsOf(pull).get(2)).body(),
            Matchers.containsString(
                QnMergeTest.PHRASES.getString(
                    "QnMerge.system-files-affected"
                )
            )
        );
    }

    /**
     * Create a fresh pull request in a new mock repo with head/base branches.
     * @return Pull request
     * @throws IOException If fails
     */
    private static Pull pull() throws IOException {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        return repo.pulls().create("", "head", "base");
    }

    /**
     * Get the comments collection for the given pull request.
     * @param pull Pull request
     * @return Comments
     * @throws IOException If fails
     */
    private static Comments commentsOf(final Pull pull) throws IOException {
        return pull.repo().issues().get(pull.number()).comments();
    }

    /**
     * Get the (mock) CI statuses collection for the given pull request.
     * @param pull Pull request
     * @return Mock statuses
     * @throws IOException If fails
     */
    private static MkChecks statusesOf(final Pull pull) throws IOException {
        return (MkChecks) pull.checks();
    }

    /**
     * Merge request directives for the given pull.
     * @param pull Pull request
     * @return Directives
     * @throws IOException In case of error
     * @throws URISyntaxException In case of error
     */
    private static Directives mergeRequest(final Pull pull)
        throws IOException, URISyntaxException {
        return new Directives()
            .add("request").append(
                new QnMerge().understand(
                    new Comment.Smart(
                        QnMergeTest.commentsOf(pull).post(QnMergeTest.COMMAND)
                    ),
                    new URI("#")
                ).dirs()
            );
    }
}
