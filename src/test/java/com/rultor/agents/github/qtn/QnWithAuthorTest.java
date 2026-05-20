/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.agents.github.Question;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for {@link QnWithAuthor}.
 * @since 1.65
 */
final class QnWithAuthorTest {

    /**
     * QnWithAuthor can add author.
     * @throws Exception In case of error.
     */
    @Test
    void addsAuthor() throws Exception {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().create("title", "body");
        issue.comments().post("comment");
        MatcherAssert.assertThat(
            "stop request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnWithAuthor(new QnStop()).understand(
                        new Comment.Smart(issue.comments().get(1)),
                        new URI("#")
                    ).dirs()
                )
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
    void doesNotAddAuthorToEmptyReq() throws Exception {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final Issue issue = repo.issues().create("the title", "the body");
        issue.comments().post("the comment");
        MatcherAssert.assertThat(
            "Author should not be added to request",
            new Xembler(
                new Directives().add("r").append(
                    new QnWithAuthor(new QnHello()).understand(
                        new Comment.Smart(issue.comments().get(1)),
                        new URI("#url")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPaths("/r[not(author)]")
        );
    }
}
