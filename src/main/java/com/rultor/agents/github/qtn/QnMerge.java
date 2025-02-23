/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
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
 * Merge request.
 *
 * @since 1.3
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnMerge implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home
    ) throws IOException {
        final Issue.Smart issue = new Issue.Smart(comment.issue());
        final Req req;
        if (issue.isPull() && issue.isOpen()) {
            Logger.info(
                this, "merge request found in %s#%d, comment #%d",
                issue.repo().coordinates(), issue.number(), comment.number()
            );
            final CheckablePull pull = new CheckablePull(issue.pull());
            final String sysfile = ".rultor.yml";
            if (pull.containsFile(sysfile)) {
                new Answer(comment).post(
                    false,
                    String.format(
                        QnMerge.PHRASES.getString(
                            "QnMerge.system-files-affected"
                        ),
                        sysfile
                    )
                );
                req = Req.DONE;
            } else
                if (pull.allChecksSuccessful()) {
                    new Answer(comment).post(
                        true,
                        String.format(
                            QnMerge.PHRASES.getString("QnMerge.start"),
                            home.toASCIIString()
                        )
                    );
                    req = QnMerge.pack(
                        comment,
                        issue.repo().pulls().get(issue.number())
                    );
                } else {
                    new Answer(comment).post(
                        false,
                        QnMerge.PHRASES.getString("QnMerge.checks-are-failed")
                    );
                    req = Req.DONE;
                }
        } else {
            new Answer(comment).post(
                false,
                QnMerge.PHRASES.getString("QnMerge.already-closed")
            );
            req = Req.EMPTY;
        }
        return req;
    }

    /**
     * Pack a pull request.
     * @param comment The comment we're in
     * @param pull Pull
     * @return Req
     * @throws IOException If fails
     */
    @SuppressWarnings("unchecked")
    private static Req pack(final Comment.Smart comment,
        final Pull pull
    ) throws IOException {
        final PullRef head = pull.head();
        final PullRef base = pull.base();
        final Req req;
        final String repo = "repo";
        if (head.json().isNull(repo)) {
            new Answer(comment).post(
                false,
                QnMerge.PHRASES.getString("QnMerge.head-is-gone")
            );
            req = Req.EMPTY;
        } else if (base.json().isNull(repo)) {
            new Answer(comment).post(
                false,
                QnMerge.PHRASES.getString("QnMerge.base-is-gone")
            );
            req = Req.EMPTY;
        } else {
            req = new Req.Simple(
                "merge",
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
        }
        return req;
    }

}
