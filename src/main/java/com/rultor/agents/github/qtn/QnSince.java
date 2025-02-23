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
 * Question asked after given Github message number.
 *
 * @since 1.6.5
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "number", "origin" })
public final class QnSince implements Question {

    /**
     * Message number.
     */
    private final transient int number;

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param num Message number
     * @param qtn Original question
     */
    public QnSince(final int num, final Question qtn) {
        this.number = num;
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        if (comment.number() > this.number || comment.number() < 10) {
            req = this.origin.understand(comment, home);
        } else {
            req = Req.EMPTY;
        }
        return req;
    }

}
