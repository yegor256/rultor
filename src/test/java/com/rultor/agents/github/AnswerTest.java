/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import java.io.IOException;
import java.util.Date;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link Answer}.
 *
 * @since 1.8.16
 */
final class AnswerTest {

    /**
     * Answer can post a message.
     * @throws Exception In case of error.
     */
    @Test
    void postsGitHubComment() throws Exception {
        final Issue issue = AnswerTest.issue();
        issue.comments().post("hey, do it");
        new Answer(new Comment.Smart(issue.comments().get(1))).post(
            true, "hey you\u0000"
        );
        MatcherAssert.assertThat(
            "Answer with source comment should be posted",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString("> hey, do it\n\n")
        );
    }

    /**
     * Answer can reject a message if it's a spam from us.
     * @throws Exception In case of error.
     */
    @Test
    void preventsSpam() throws Exception {
        final Issue issue = AnswerTest.issue();
        ((MkGitHub) issue.repo().github()).relogin("walter")
            .repos().get(issue.repo().coordinates())
            .issues().get(1).comments().post("hello, how are you?");
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().get(1)
        );
        final Answer answer = new Answer(comment);
        for (int idx = 0; idx < 10; ++idx) {
            answer.post(true, "oops");
        }
        MatcherAssert.assertThat(
            "Only 5 answers should be posted",
            new ListOf<>(issue.comments().iterate(new Date(0L))).size(),
            Matchers.is(6)
        );
    }

    /**
     * Make an issue.
     * @return Issue
     * @throws IOException If fails
     */
    private static Issue issue() throws IOException {
        final Repo repo = new MkGitHub().randomRepo();
        return repo.issues().create("", "");
    }

}
