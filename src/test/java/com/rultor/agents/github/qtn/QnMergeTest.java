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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnMerge}.
 *
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
     * All pull request comments.
     */
    private transient Comments comments;

    /**
     * Pull request.
     */
    private transient Pull pull;

    /**
     * Initial phase for all tests.
     * @throws IOException In case of error.
     */
    @BeforeEach
    void setUp() throws IOException {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        final String head = "head";
        final String base = "base";
        branches.create(head, "abcdef4");
        branches.create(base, "abcdef5");
        this.pull = repo.pulls().create("", head, base);
        this.comments = repo.issues()
            .get(this.pull.number())
            .comments();
    }

    /**
     * QnMerge can build a request.
     *
     * @throws Exception In case of error
     */
    @Test
    void buildsRequest() throws Exception {
        final String request = new Xembler(this.mergeRequest()).xml();
        MatcherAssert.assertThat(
            "Merge request should be created",
            request,
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
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(this.comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
        MatcherAssert.assertThat(
            "Comment about staring merge should be posted",
            new Comment.Smart(this.comments.get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnMergeTest.PHRASES.getString("QnMerge.start"),
                    "#"
                )
            )
        );
    }

    /**
     * QnMerge can not build a request because some GitHub checks
     *  were failed.
     *
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void stopsBecauseCiChecksFailed()
        throws IOException, URISyntaxException {
        final MkChecks checks = (MkChecks) this.pull.checks();
        checks.create(Check.Status.IN_PROGRESS, Check.Conclusion.SUCCESS);
        this.mergeRequest();
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(this.comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
        MatcherAssert.assertThat(
            "Merge should be stopped if checks are not successful",
            new Comment.Smart(this.comments.get(2)).body(),
            Matchers.containsString(
                QnMergeTest.PHRASES.getString("QnMerge.checks-are-failed")
            )
        );
    }

    /**
     * QnMerge can build a request because GitHub checks finished successfully.
     *
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    void continuesBecauseCiChecksSuccessful()
        throws IOException, URISyntaxException {
        final MkChecks checks = (MkChecks) this.pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        this.mergeRequest();
        MatcherAssert.assertThat(
            "Merge comment should be initiator",
            new Comment.Smart(this.comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
        MatcherAssert.assertThat(
            "Merge start info comment should be posted",
            new Comment.Smart(this.comments.get(2)).body(),
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
        final MkChecks checks = (MkChecks) this.pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        final List<JsonObject> files = new LinkedList<>();
        files.add(Json.createObjectBuilder()
            .add("sha", "ef36558cbd")
            .add("filename", "README.md")
            .add("status", "modified")
            .build()
        );
        files.add(Json.createObjectBuilder()
            .add("sha", "ef3857cad")
            .add("filename", ".rultor.yml")
            .add("status", "modified")
            .build()
        );
        this.mergeRequest();
        MatcherAssert.assertThat(
            "Comment should be posted about affected system file",
            new Comment.Smart(this.comments.get(2)).body(),
            Matchers.containsString(
                QnMergeTest.PHRASES.getString(
                    "QnMerge.system-files-affected"
                )
            )
        );
    }

    /**
     * Merge request directives.
     * @return Directives
     * @throws IOException In case of error
     * @throws URISyntaxException In case of error
     */
    private Directives mergeRequest() throws IOException,
        URISyntaxException {
        return new Directives()
            .add("request")
            .append(
                new QnMerge().understand(
                    new Comment.Smart(
                        this.comments.post(QnMergeTest.COMMAND)
                    ),
                    new URI("#")
                ).dirs()
            );
    }
}
