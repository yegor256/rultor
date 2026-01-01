/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnHello}.
 *
 * @since 1.6
 */
final class QnHelloTest {

    /**
     * QnHello can reply.
     * @throws Exception In case of error.
     */
    @Test
    void repliesInGitHub() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hello");
        MatcherAssert.assertThat(
            "Request should be marked as done",
            new QnHello().understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Hello message should be posted",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString("Have fun :)")
        );
    }

}
