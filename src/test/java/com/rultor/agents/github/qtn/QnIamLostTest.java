/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnIamLost}.
 *
 * @since 1.60
 */
final class QnIamLostTest {

    /**
     * QnIamLost can build a report.
     * @throws Exception In case of error.
     */
    @Test
    void saySomethingBack() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("boom");
        MatcherAssert.assertThat(
            "Command should be marked as done",
            new QnIamLost().understand(
                new Comment.Smart(issue.comments().get(1)),
                new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Comment about not found command should be posted",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString("don't understand you")
        );
    }

}
