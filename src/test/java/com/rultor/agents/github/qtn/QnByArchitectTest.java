/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.User;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Question;
import com.rultor.spi.Profile;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link QnByArchitect}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.45
 */
public final class QnByArchitectTest {

    /**
     * QnByArchitect can reject if not an architect.
     * @throws Exception In case of error.
     */
    @Test
    public void rejectsIfNotArchitect() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
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
            issue.comments().iterate(new Date(0L)),
            Matchers.iterableWithSize(2)
        );
    }

    /**
     * QnByArchitect can accept if an architect.
     * @throws Exception In case of error.
     */
    @Test
    public void acceptsIfArchitect() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
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
    @Disabled
    @Test
    public void acceptsIfMergeArchitectPull() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
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
