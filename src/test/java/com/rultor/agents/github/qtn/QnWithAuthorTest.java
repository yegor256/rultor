/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for {@link QnWithAuthor}.
 *
 * @since 1.65
 */
final class QnWithAuthorTest {

    /**
     * QnWithAuthor can add author.
     * @throws Exception In case of error.
     */
    @Test
    void addsAuthor() throws Exception {
        final MkGithub github = new MkGithub();
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().create("title", "body");
        issue.comments().post("comment");
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().get(1)
        );
        final Question question = new QnWithAuthor(
            new QnStop()
        );
        final Req req = question.understand(comment, new URI("#"));
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("request").append(req.dirs())
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/request[type='stop']",
                "/request/args[not(arg)]",
                "/request/author"
            )
        );
    }

    /**
     * QnWithAuthor can ignore if Req is empty.
     * @throws Exception In case of error.
     */
    @Test
    void doesntAddAuthorToEmptyReq() throws Exception {
        final MkGithub github = new MkGithub();
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().create("the title", "the body");
        issue.comments().post("the comment");
        final Comment.Smart comment = new Comment.Smart(
            issue.comments().get(1)
        );
        final Question question = new QnWithAuthor(new QnHello());
        final Req req = question.understand(comment, new URI("#url"));
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("r").append(req.dirs())
            ).xml(),
            XhtmlMatchers.hasXPaths("/r[not(author)]")
        );
    }

}
