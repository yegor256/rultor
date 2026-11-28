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
import java.util.Locale;
import java.util.regex.Pattern;
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
     * Pattern to count mentions of the commander in the answer.
     */
    private static final Pattern MENTION =
        Pattern.compile("@user2", Pattern.LITERAL);

    /**
     * QnAskedBy can exclude {@code @rultor} from list of commanders (#690).
     *
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
        MatcherAssert.assertThat(
            "Rultor should not be included in the message",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.not(
                Matchers.containsString("@rultor")
            )
        );
    }

    /**
     * QnAskedBy can include all architects in the list of commanders.
     *
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

    /**
     * QnAskedBy can list a login only once when it is present in both
     * the commanders of the command and the architects (#1085).
     *
     * @throws Exception In case of error.
     */
    @Test
    void doesNotDuplicateLoginInCommandersAndArchitect() throws Exception {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().create("needs", "deployment");
        final String action = "deploy";
        issue.comments().post(action);
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().get(1)
        );
        final QnAskedBy qab = new QnAskedBy(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='architect'><item>user2</item></entry>",
                        "<entry key='deploy'><entry key='commanders'>",
                        "<item>user2</item></entry></entry></p>"
                    ).asString()
                )
            ),
            String.format(
                "/p/entry[@key='%s']/entry[@key='commanders']/item/text()",
                action
            ),
            Mockito.mock(Question.class)
        );
        qab.understand(comment, new URI("#1"));
        MatcherAssert.assertThat(
            "A login listed twice must appear once in the message",
            QnAskedByTest.MENTION.split(
                new Comment.Smart(issue.comments().get(2)).body(), -1
            ).length - 1,
            Matchers.equalTo(1)
        );
    }

    /**
     * QnAskedBy can treat logins that differ only in letter case as one
     * commander, since GitHub logins are case-insensitive (#1085).
     *
     * @throws Exception In case of error.
     */
    @Test
    void treatsLoginsDifferingOnlyInCaseAsOneCommander() throws Exception {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().create("needs", "deployment");
        final String action = "deploy";
        issue.comments().post(action);
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().get(1)
        );
        final QnAskedBy qab = new QnAskedBy(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='architect'><item>user2</item></entry>",
                        "<entry key='deploy'><entry key='commanders'>",
                        "<item>User2</item></entry></entry></p>"
                    ).asString()
                )
            ),
            String.format(
                "/p/entry[@key='%s']/entry[@key='commanders']/item/text()",
                action
            ),
            Mockito.mock(Question.class)
        );
        qab.understand(comment, new URI("#1"));
        MatcherAssert.assertThat(
            "Logins differing only in case must be listed once",
            QnAskedByTest.MENTION.split(
                new Comment.Smart(issue.comments().get(2)).body()
                    .toLowerCase(Locale.ROOT), -1
            ).length - 1,
            Matchers.equalTo(1)
        );
    }
}
