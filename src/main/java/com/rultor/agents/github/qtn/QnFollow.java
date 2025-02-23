/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Question asked by one of them.
 *
 * @since 1.43.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnFollow implements Question {

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnFollow(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        comment.issue().repo().github().entry().uri()
            .path("/user/following")
            .path(comment.author().login())
            .back()
            .method(Request.PUT)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_NO_CONTENT);
        Logger.info(
            this, "GitHub user @%s followed",
            comment.author().login()
        );
        return this.origin.understand(comment, home);
    }

}
