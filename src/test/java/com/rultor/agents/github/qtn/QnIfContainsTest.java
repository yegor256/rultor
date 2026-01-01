/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import java.net.URI;
import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnIfContains}.
 *
 * @since 1.50
 */
final class QnIfContainsTest {

    /**
     * QnIfContains can block a request.
     * @throws Exception In case of error.
     */
    @Test
    void blocksRequest() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("something");
        new QnIfContains("hello", new QnHello()).understand(
            new Comment.Smart(issue.comments().get(1)), new URI("#")
        ).dirs();
        MatcherAssert.assertThat(
            "No message should be posted",
            issue.comments().iterate(new Date(0L)),
            Matchers.iterableWithSize(1)
        );
    }

    /**
     * QnIfContains can allow a request.
     * @throws Exception In case of error.
     */
    @Test
    void allowsRequest() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("something else to MErge");
        new QnIfContains("merge", new QnHello()).understand(
            new Comment.Smart(issue.comments().get(1)), new URI("#test")
        ).dirs();
        MatcherAssert.assertThat(
            "Hello message should be posted",
            issue.comments().iterate(new Date(0L)),
            Matchers.iterableWithSize(2)
        );
    }

    /**
     * QnIfContains skips the content in ``.
     * @throws Exception In case of error.
     */
    @Test
    void ignoreQuotedCommandsRequest() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("`another version` to release");
        new QnIfContains("version", new QnHello()).understand(
            new Comment.Smart(issue.comments().get(1)), new URI("#")
        ).dirs();
        MatcherAssert.assertThat(
            "No comments should be posted",
            issue.comments().iterate(new Date(0L)),
            Matchers.iterableWithSize(1)
        );
    }
}
