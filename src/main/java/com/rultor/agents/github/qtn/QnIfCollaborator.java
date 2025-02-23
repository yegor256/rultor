/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Repo;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * If rultor is in the list of collaborators.
 *
 * @since 1.7
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnIfCollaborator implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnIfCollaborator(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final Repo repo = comment.issue().repo();
        final String self = repo.github().users().self().login();
        final Collection<String> crew = new Crew(repo).names();
        if (crew.isEmpty() || crew.contains(self)) {
            req = this.origin.understand(comment, home);
        } else {
            new Answer(comment).post(
                false,
                QnIfCollaborator.PHRASES.getString("QnIfCollaborator.denied")
            );
            req = Req.DONE;
        }
        return req;
    }

}
