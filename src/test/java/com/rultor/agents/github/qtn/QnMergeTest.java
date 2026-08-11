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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
     * QnMerge can build a request.
     * @throws Exception In case of error
     */
    @Test
    void buildsRequest() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        MatcherAssert.assertThat(
            "Merge request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnMerge().understand(
                        new Comment.Smart(
                            repo.issues().get(
                                repo.pulls().create("", "head", "base")
                                    .number()
                            ).comments().post(QnMergeTest.COMMAND)
                        ),
                        new URI("#")
                    ).dirs()
                )
            ).xml(),
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
     * QnMerge posts the initiator command as the first comment.
     * @throws Exception In case of error
     */
    @Test
    void postsInitiatorCommentWhenMergeRequested() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Comments comments = repo.issues().get(
            repo.pulls().create("", "head", "base").number()
        ).comments();
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
    }

    /**
     * QnMerge posts a comment about starting the merge.
     * @throws Exception In case of error
     */
    @Test
    void postsStartCommentWhenMergeRequested() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Comments comments = repo.issues().get(
            repo.pulls().create("", "head", "base").number()
        ).comments();
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Comment about staring merge should be posted",
            new Comment.Smart(comments.get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnMergeTest.PHRASES.getString("QnMerge.start"),
                    "#"
                )
            )
        );
    }

    /**
     * QnMerge posts the initiator comment even while a GitHub check is
     * still in progress.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void postsInitiatorCommentWhenChecksInProgress()
        throws IOException, URISyntaxException {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        final Comments comments = repo.issues().get(pull.number()).comments();
        final MkChecks checks = (MkChecks) pull.checks();
        checks.create(Check.Status.IN_PROGRESS, Check.Conclusion.SUCCESS);
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
    }

    /**
     * QnMerge can not build a request because some GitHub checks
     * were failed.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void stopsBecauseCiChecksFailed() throws IOException, URISyntaxException {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        final Comments comments = repo.issues().get(pull.number()).comments();
        final MkChecks checks = (MkChecks) pull.checks();
        checks.create(Check.Status.IN_PROGRESS, Check.Conclusion.SUCCESS);
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Merge should be stopped if checks are not successful",
            new Comment.Smart(comments.get(2)).body(),
            Matchers.containsString(
                QnMergeTest.PHRASES.getString("QnMerge.checks-are-failed")
            )
        );
    }

    /**
     * QnMerge posts the initiator comment when GitHub checks finished
     * successfully.
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void postsInitiatorCommentWhenChecksSuccessful()
        throws IOException, URISyntaxException {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        final Comments comments = repo.issues().get(pull.number()).comments();
        final MkChecks checks = (MkChecks) pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(comments.get(1)).body(),
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
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        final Comments comments = repo.issues().get(pull.number()).comments();
        final MkChecks checks = (MkChecks) pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Merge start info comment should be posted",
            new Comment.Smart(comments.get(2)).body(),
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
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        final Comments comments = repo.issues().get(pull.number()).comments();
        final MkChecks checks = (MkChecks) pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SKIPPED);
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Merge should proceed when some checks are skipped",
            new Comment.Smart(comments.get(2)).body(),
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
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        final Comments comments = repo.issues().get(pull.number()).comments();
        final MkChecks checks = (MkChecks) pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        new Directives().add("request").append(
            new QnMerge().understand(
                new Comment.Smart(comments.post(QnMergeTest.COMMAND)),
                new URI("#")
            ).dirs()
        );
        MatcherAssert.assertThat(
            "Comment should be posted about affected system file",
            new Comment.Smart(comments.get(2)).body(),
            Matchers.containsString(
                QnMergeTest.PHRASES.getString(
                    "QnMerge.system-files-affected"
                )
            )
        );
    }
}
