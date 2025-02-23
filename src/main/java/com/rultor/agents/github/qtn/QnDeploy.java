/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
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
 * Deploy request.
 *
 * @since 1.3
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnDeploy implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(
        final Comment.Smart comment,
        final URI home
    ) throws IOException {
        new Answer(comment).post(
            true,
            String.format(
                QnDeploy.PHRASES.getString("QnDeploy.start"),
                home.toASCIIString()
            )
        );
        final Issue issue = comment.issue();
        final Repo repo = issue.repo();
        Logger.info(
            this, "deploy request found in %s#%d comment #%d",
            repo.coordinates(), issue.number(), comment.number()
        );
        return new Req.Simple(
            "deploy",
            new MapOf<String, String>(
                new MapEntry<>(
                    "head_branch", new DefaultBranch(repo).toString()
                ),
                new MapEntry<>(
                    "head",
                    String.format(
                        "git@github.com:%s.git",
                        repo.coordinates()
                    )
                )
            )
        );
    }

}
