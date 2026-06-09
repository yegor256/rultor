/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.mock.MkGitHub;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link FirstComment}.
 * @since 1.51
 */
final class FirstCommentTest {

    /**
     * FirstComment can parse an issue.
     * @throws Exception In case of error.
     */
    @Test
    void parsesGitHubIssue() throws Exception {
        MatcherAssert.assertThat(
            "Author should be added",
            new Comment.Smart(
                new FirstComment(
                    new Issue.Smart(
                        new MkGitHub().randomRepo().issues().create("", "")
                    )
                )
            ).author().login(),
            Matchers.equalTo("jeff")
        );
    }
}
