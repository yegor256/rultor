/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Mark this question in GitHub with an emoji, to show the
 * author that the comments has been seen.
 *
 * @since 1.50.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnReaction implements Question {

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnReaction(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final String emoji = "heart";
        final Response res = comment.issue().repo().github().entry().uri()
            .path("/repos")
            .path(comment.issue().repo().coordinates().user())
            .path(comment.issue().repo().coordinates().repo())
            .path("issues/comments")
            .path(Long.toString(comment.number()))
            .path("reactions")
            .back()
            .method(Request.POST)
            .body()
            .set(String.format("{\"content\": \"%s\"}", emoji))
            .back()
            .fetch();
        if (res.status() == HttpURLConnection.HTTP_CREATED) {
            Logger.info(
                this, "Emoji '%s' to GitHub comment #%d in %s",
                emoji,
                comment.number(),
                comment.issue().repo().coordinates()
            );
        }
        return this.origin.understand(comment, home);
    }

}
