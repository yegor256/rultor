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
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Question.
 *
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "login", "origin" })
public final class QnReferredTo implements Question {

    /**
     * Regex that matches the @ and Rultor user login in GitHub comments.
     * Matches the login when at the beginning of the comment string or when
     * preceded by a space or comma.
     * Login has to be bound by a word boundary to the right.
     * Only captures the @ sign and login.
     */
    private static final String MENTION_MATCHER =
        "(?:^|(?:.*?(?:\\s|,)))(%s)\\b.*?";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * My login.
     */
    private final transient String login;

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param self Self login
     * @param qtn Original question
     */
    public QnReferredTo(final String self, final Question qtn) {
        this.login = self;
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final String prefix = String.format("@%s", this.login);
        final Req req;
        final Matcher matcher = Pattern.compile(
            String.format(QnReferredTo.MENTION_MATCHER, prefix)
        ).matcher(comment.body().trim());
        if (matcher.matches()) {
            if (matcher.start(1) == 0) {
                req = this.origin.understand(comment, home);
            } else {
                new Answer(comment).post(
                    true,
                    String.format(
                        QnReferredTo.PHRASES.getString(
                            "QnReferredTo.mentioned"
                        ),
                        prefix
                    )
                );
                Logger.info(
                    this, "mention found in #%d", comment.issue().number()
                );
                req = Req.DONE;
            }
        } else {
            Logger.info(
                this,
                "Comment #%d in %s#%d is not for me (no \"%s\" prefix)",
                comment.number(), comment.issue().repo().coordinates(),
                comment.issue().number(), prefix
            );
            req = Req.EMPTY;
        }
        return req;
    }
}
