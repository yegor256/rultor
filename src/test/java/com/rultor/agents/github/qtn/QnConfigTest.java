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
import com.rultor.spi.Profile;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnConfig}.
 *
 * @since 1.8
 */
final class QnConfigTest {

    /**
     * QnConfig can reply.
     * @throws Exception In case of error.
     */
    @Test
    void repliesInGitHub() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hello");
        final Profile profile = new Profile.Fixed();
        MatcherAssert.assertThat(
            "hello command should be recognized",
            new QnConfig(profile).understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Xml view of rultor.yml should be added to the comment",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString("</p>")
        );
    }

}
