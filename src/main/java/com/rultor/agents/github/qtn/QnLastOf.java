/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.immutable.Array;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Last of.
 *
 * @since 1.6.5
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "questions")
public final class QnLastOf implements Question {

    /**
     * Original questions.
     */
    private final transient Array<Question> questions;

    /**
     * Ctor.
     * @param qtns Original questions
     */
    public QnLastOf(final Iterable<Question> qtns) {
        this.questions = new Array<>(qtns);
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        Req req = Req.EMPTY;
        for (final Question qtn : this.questions) {
            final Req next = qtn.understand(comment, home);
            if (!next.equals(Req.EMPTY)) {
                req = next;
            }
        }
        return req;
    }
}
