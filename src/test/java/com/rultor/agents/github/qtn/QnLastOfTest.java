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
 * Tests for ${@link QnLastOf}.
 *
 * @since 1.6.5
 */
final class QnLastOfTest {

    /**
     * QnLastOf can get the last one.
     * @throws Exception In case of error.
     */
    @Test
    void getsLastReq() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Comment comment = issue.comments().post("deploy");
        MatcherAssert.assertThat(
            "Deploy request should be created",
            new QnLastOf(
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
