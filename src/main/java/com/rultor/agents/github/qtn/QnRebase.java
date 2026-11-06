/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Pull;
import com.jcabi.github.PullRef;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;

/**
 * Rebase request.
 *
 * @since 2.1
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnRebase implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Constructor.
     */
    public QnRebase() {
        // Nothing to initialize.
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home
    ) throws IOException {
        final Issue.Smart issue = new Issue.Smart(comment.issue());
        final Req req;
        if (issue.isPull() && issue.isOpen()) {
            Logger.info(
                this, "rebase request found in %s#%d, comment #%d",
                issue.repo().coordinates(), issue.number(), comment.number()
            );
            new Answer(comment).post(
                true,
                String.format(
                    QnRebase.PHRASES.getString("QnRebase.start"),
                    home.toASCIIString()
                )
            );
            req = QnRebase.pack(
                comment,
                issue.repo().pulls().get(issue.number())
            );
        } else {
            new Answer(comment).post(
                false,
                QnRebase.PHRASES.getString("QnRebase.already-closed")
            );
            req = Req.EMPTY;
        }
        return req;
    }

    @SuppressWarnings("unchecked")
    private static Req pack(final Comment.Smart comment,
        final Pull pull
    ) throws IOException {
        final PullRef head = pull.head();
        final PullRef base = pull.base();
        final Req req;
        if (head.json().isNull("repo")) {
            new Answer(comment).post(
                false,
                QnRebase.PHRASES.getString("QnRebase.head-is-gone")
            );
            req = Req.EMPTY;
        } else if (head.repo().coordinates()
            .equals(base.repo().coordinates())) {
            req = new Req.Simple(
                "rebase",
                new MapOf<>(
                    new MapEntry<>(
                        "pull_id",
                        Integer.toString(pull.number())
                    ),
                    new MapEntry<>(
                        "pull_title",
                        new Issue.Smart(comment.issue()).title()
                    ),
                    new MapEntry<>(
                        "fork_branch",
                        head.ref()
                    ),
                    new MapEntry<>(
                        "head_branch",
                        base.ref()
                    ),
                    new MapEntry<>(
                        "head",
                        String.format(
                            "git@github.com:%s.git",
                            base.repo().coordinates().toString()
                        )
                    ),
                    new MapEntry<>(
                        "fork",
                        String.format(
                            "git@github.com:%s.git",
                            head.repo().coordinates().toString()
                        )
                    )
                )
            );
        } else {
            new Answer(comment).post(
                false,
                QnRebase.PHRASES.getString("QnRebase.fork-not-supported")
            );
            req = Req.DONE;
        }
        return req;
    }
}
