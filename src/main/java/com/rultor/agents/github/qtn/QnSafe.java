/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * It never fails.
 *
 * @since 1.57
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
@SuppressWarnings("PMD.AvoidCatchingThrowable")
public final class QnSafe implements Question {

    /**
     * Default error message format.
     */
    private static final String DEFAULT_FORMAT =
        "We failed, sorry, try again:\n\n```\n%[exception]s\n```";

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnSafe(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home
    ) throws IOException {
        Req req;
        if (QnSafe.valid(comment)) {
            try {
                req = this.origin.understand(comment, home);
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Throwable ex) {
                new Answer(comment).post(
                    false,
                    Logger.format(
                        QnSafe.DEFAULT_FORMAT,
                        ex
                    )
                );
                req = Req.DONE;
            }
        } else {
            req = Req.EMPTY;
        }
        return req;
    }

    /**
     * Is it a valid issue?
     * @param comment The comment
     * @return TRUE if valid
     */
    private static boolean valid(final Comment.Smart comment) {
        boolean valid = true;
        try {
            comment.issue().json();
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            valid = false;
            Logger.warn(QnSafe.class, "%[exception]s", ex);
        }
        return valid;
    }

}
