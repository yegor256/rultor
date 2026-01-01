/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Stop a task.
 * @since 1.1
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnStop implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        new Answer(comment).post(
            true,
            String.format(
                QnStop.PHRASES.getString("QnStop.stop"),
                home.toASCIIString()
            )
        );
        final Issue issue = comment.issue();
        Logger.info(
            this, "stop request found in %s#%d comment #%d",
            issue.repo().coordinates(), issue.number(), comment.number()
        );
        return new Req.Simple(
            "stop", Collections.emptyMap()
        );
    }

}
