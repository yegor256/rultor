/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
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
import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * First of.
 *
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "questions")
public final class QnFirstOf implements Question {

    /**
     * Original questions.
     */
    private final transient Array<Question> questions;

    /**
     * Ctor.
     * @param qtns Original questions
     */
    public QnFirstOf(final Question... qtns) {
        this(Arrays.asList(qtns));
    }

    /**
     * Ctor.
     * @param qtns Original questions
     */
    public QnFirstOf(final Iterable<Question> qtns) {
        this.questions = new Array<>(qtns);
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        Req req = Req.EMPTY;
        for (final Question qtn : this.questions) {
            req = qtn.understand(comment, home);
            if (!req.equals(Req.EMPTY)) {
                break;
            }
        }
        return req;
    }
}
