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
import com.jcabi.github.mock.MkGithub;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Question;
import com.rultor.spi.Profile;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link QnAskedBy}.
 *
 * @author Nathan Green (ngreen@inco5.com)
 * @version $Id$
 */
public final class QnAskedByTest {

    /**
     * QnAskedBy can exclude {@code @rultor} from list of commanders (#690).
     * @throws Exception In case of error.
     */
    @Test
    public void excludesRultorFromListOfCommanders() throws Exception {
        final MkGithub github = new MkGithub();
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
    public void includesArchitectsInListOfCommanders() throws Exception {
        final MkGithub github = new MkGithub();
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
                    String.format(
                        StringUtils.join(
                            "<p><entry key='architect'><item>%s</item>",
                            "<item>fooo</item></entry></p>"
                        ),
                        repo.github().users().self().login()
                    )
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
