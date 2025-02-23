/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.Env;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnVersion}.
 *
 * @since 1.6
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class QnVersionTest {

    /**
     * QnVersion can reply.
     * @throws Exception In case of error.
     */
    @Test
    void repliesInGithub() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("version");
        MatcherAssert.assertThat(
            "Request should be marked as done",
            new QnVersion().understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Version should be printed",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString("My current version is")
        );
    }

    /**
     * QnVersion reply contains link to revision.
     * @throws Exception In case of error.
     */
    @Test
    void repliesWithLinkToRevision() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("version");
        MatcherAssert.assertThat(
            "Request should be marked as done",
            new QnVersion().understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Link to the revision should be posted",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                String.format(
                    "/commit/%s",
                    Env.read("Rultor-Revision")
                )
            )
        );
    }

}
