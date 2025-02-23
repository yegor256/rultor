/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Question asked NOT by myself.
 *
 * @since 1.46.7
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnNotSelf implements Question {

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnNotSelf(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final String self = comment.issue().repo()
            .github().users().self().login();
        if (self.equals(comment.author().login())) {
            req = Req.EMPTY;
        } else {
            req = this.origin.understand(comment, home);
        }
        return req;
    }

}
