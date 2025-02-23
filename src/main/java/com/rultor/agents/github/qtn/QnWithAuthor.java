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
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Question that identifies an author.
 *
 * @since 1.65
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnWithAuthor implements Question {

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnWithAuthor(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req = this.origin.understand(comment, home);
        final String author = comment.author()
            .login()
            .toLowerCase(Locale.ENGLISH);
        final Iterable<Directive> dirs = req.dirs();
        final Req out;
        if (dirs.iterator().hasNext()) {
            out = new Req() {
                @Override
                public Iterable<Directive> dirs() {
                    return new Directives()
                        .add("author").set(author).up()
                        .append(dirs);
                }
            };
        } else {
            out = req;
        }
        return out;
    }

}
