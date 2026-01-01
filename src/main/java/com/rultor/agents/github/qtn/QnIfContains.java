/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * If contains text.
 *
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "pattern", "origin" })
public final class QnIfContains implements Question {

    /**
     * Text to search.
     */
    private final transient String pattern;

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param ptn Pattern to search for
     * @param qtn Original question
     */
    public QnIfContains(final String ptn, final Question qtn) {
        this.pattern = ptn.toLowerCase(Locale.ENGLISH);
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        if (comment.body().toLowerCase(Locale.ENGLISH)
            .replaceAll("`[^`]*`", "")
            .contains(this.pattern)
        ) {
            req = this.origin.understand(comment, home);
        } else {
            req = Req.EMPTY;
        }
        return req;
    }
}
