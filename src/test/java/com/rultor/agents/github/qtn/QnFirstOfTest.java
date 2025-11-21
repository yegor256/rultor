/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.net.URI;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnFirstOf}.
 *
 * @since 1.6.5
 */
final class QnFirstOfTest {

    /**
     * QnFirstOf can get the first one.
     * @throws Exception In case of error.
     */
    @Test
    void getsFirstReq() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Comment comment = issue.comments().post("deploy");
        MatcherAssert.assertThat(
            "First not empty question should be taken",
            new QnFirstOf(
                Arrays.asList(
                    Question.EMPTY,
                    new QnDeploy(),
                    Question.EMPTY
                )
            ).understand(new Comment.Smart(comment), new URI("#")),
            Matchers.not(Matchers.equalTo(Req.EMPTY))
        );
    }

}
