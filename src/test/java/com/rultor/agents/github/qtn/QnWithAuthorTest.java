/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
            "stop request should be created",
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
            "Author should not be added to request",
            new Xembler(
                new Directives().add("r").append(req.dirs())
            ).xml(),
            XhtmlMatchers.hasXPaths("/r[not(author)]")
        );
    }

}
