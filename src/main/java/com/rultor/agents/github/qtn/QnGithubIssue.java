/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.cactoos.list.ListOf;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Question that passes github_issue as env variable.
 *
 * @since 2.0
 */
public final class QnGithubIssue implements Question {

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param question Original question
     */
    public QnGithubIssue(final Question question) {
        this.origin = question;
    }

    @Override
    public Req understand(final Comment.Smart comment, final URI home)
        throws IOException {
        Req req = this.origin.understand(comment, home);
        final List<Directive> additions = new ListOf<>(req.dirs());
        if (!additions.isEmpty()) {
            final Directives dirs = new Directives().append(additions);
            req = () -> dirs.addIf("args")
                .add("arg").attr("name", "github_issue")
                .set(String.valueOf(comment.issue().number()))
                .up().up();
        }
        return req;
    }
}
