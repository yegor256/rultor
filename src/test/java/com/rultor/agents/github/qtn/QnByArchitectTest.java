/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.User;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Question;
import com.rultor.spi.Profile;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link QnByArchitect}.
 *
 * @since 1.45
 */
final class QnByArchitectTest {

    /**
     * QnByArchitect can reject if not an architect.
     * @throws Exception In case of error.
     */
    @Test
    void rejectsIfNotArchitect() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().post("deploy")
        );
        final Question question = Mockito.mock(Question.class);
        final URI home = new URI("#");
        new QnByArchitect(
            new Profile.Fixed(
                new XMLDocument("<p><entry key='a'>johnny</entry></p>")
            ),
            "/p/entry[@key='a']/text()", question
        ).understand(comment, home);
        Mockito.verify(question, Mockito.never()).understand(comment, home);
        MatcherAssert.assertThat(
            "Two comments should be posted",
            issue.comments().iterate(new Date(0L)),
            Matchers.iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            "Confirmation request comment should be posted",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                "Thanks for your request; @johnny please confirm this"
            )
        );
    }

    /**
     * QnByArchitect can accept if an architect.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsIfArchitect() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().post("release")
        );
        final Question question = Mockito.mock(Question.class);
        final URI home = new URI("#1");
        new QnByArchitect(
            new Profile.Fixed(
                new XMLDocument(
                    String.format(
                        "<p><entry key='b'>%s</entry></p>",
                        repo.github().users().self().login().toUpperCase(
                            Locale.ENGLISH
                        )
                    )
                )
            ),
            "/p/entry[@key='b']/text()", question
        ).understand(comment, home);
        Mockito.verify(question).understand(comment, home);
    }

    /**
     * QnByArchitect can accept if is a merge request made by anyone in a
     * pull request made by an architect.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsIfMergeArchitectPull() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final User author = Mockito.mock(User.class);
        Mockito.when(author.login()).thenReturn(
            repo.github().users().self().login().toUpperCase(
                Locale.ENGLISH
            )
        );
        final Issue.Smart issue = Mockito.mock(Issue.Smart.class);
        Mockito.when(issue.author()).thenReturn(author);
        Mockito.when(issue.isPull()).thenReturn(true);
        final User reviewer = Mockito.mock(User.class);
        Mockito.when(reviewer.login()).thenReturn("alfred");
        final Comment.Smart comment = Mockito.mock(Comment.Smart.class);
        Mockito.when(comment.body()).thenReturn("merge");
        Mockito.when(comment.author()).thenReturn(reviewer);
        final Question question = Mockito.mock(Question.class);
        final URI home = new URI("#2");
        new QnByArchitect(
            new Profile.Fixed(
                new XMLDocument("<p><entry key='c'>alfred</entry></p>")
            ),
            "/p/entry[@key='c']/text()", question
        ).understand(comment, home);
        Mockito.verify(question).understand(comment, home);
    }

}
