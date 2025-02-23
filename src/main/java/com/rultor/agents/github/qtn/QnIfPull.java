/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * If pull.
 *
 * @since 1.57
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnIfPull implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnIfPull(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Issue.Smart issue = new Issue.Smart(comment.issue());
        final Req req;
        if (issue.isPull()) {
            req = this.origin.understand(comment, home);
        } else {
            new Answer(comment).post(
                false,
                QnIfPull.PHRASES.getString("QnIfPull.not-pull-request")
            );
            req = Req.EMPTY;
        }
        return req;
    }

}
