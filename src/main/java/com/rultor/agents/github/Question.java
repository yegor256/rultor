/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import java.io.IOException;
import java.net.URI;

/**
 * Question.
 *
 * @since 1.3
 */
@Immutable
public interface Question {

    /**
     * Empty always.
     */
    Question EMPTY = new Question() {
        @Override
        public Req understand(final Comment.Smart comment, final URI home) {
            return Req.EMPTY;
        }
    };

    /**
     * Understand it and return the request.
     * @param comment The comment
     * @param home Home URI of the daemon
     * @return Request (or Req.EMPTY is nothing found)
     * @throws IOException If fails
     */
    Req understand(Comment.Smart comment, URI home) throws IOException;

}
