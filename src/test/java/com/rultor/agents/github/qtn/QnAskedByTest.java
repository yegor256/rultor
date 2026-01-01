/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Question;
import com.rultor.spi.Profile;
import java.net.URI;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link QnAskedBy}.
 *
 * @since 1.1
 */
final class QnAskedByTest {

    /**
     * QnAskedBy can exclude {@code @rultor} from list of commanders (#690).
     * @throws Exception In case of error.
     */
    @Test
    void excludesRultorFromListOfCommanders() throws Exception {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        repo.collaborators().add("testuser1");
        final Issue issue = repo.issues().create("title", "body");
        issue.comments().post("comment");
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().get(1)
        );
        final QnAskedBy qab = new QnAskedBy(
            new Profile.Fixed(),
            "//test",
            Mockito.mock(Question.class)
        );
        github.relogin("rultor");
        qab.understand(comment, new URI("http://localhost"));
        final Comment.Smart reply = new Comment.Smart(issue.comments().get(2));
        MatcherAssert.assertThat(
            "Rultor should not be included in the message",
            reply.body(),
            Matchers.not(
                Matchers.containsString("@rultor")
            )
        );
    }

    /**
     * QnAskedBy can include all architects in the list of commanders.
     * @throws Exception In case of error.
     */
    @Test
    void includesArchitectsInListOfCommanders() throws Exception {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        repo.collaborators().add("testuser2");
        final Issue issue = repo.issues().create("needs", "deployment");
        final String action = "deploy";
        issue.comments().post(action);
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().get(1)
        );
        final Question question = Mockito.mock(Question.class);
        final QnAskedBy qab = new QnAskedBy(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='architect'><item>",
                        repo.github().users().self().login(),
                        "</item>",
                        "<item>fooo</item></entry></p>"
                    ).asString()
                )
            ),
            String.format(
                "/p/entry[@key='%s']/entry[@key='commanders']/item/text()",
                action
            ),
            question
        );
        final URI home = new URI("#1");
        qab.understand(comment, home);
        Mockito.verify(question).understand(comment, home);
    }
}
