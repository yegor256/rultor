/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.log.Logger;
import com.rultor.Env;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Show my current version.
 *
 * @since 1.3.1
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnVersion implements Question {

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
                QnVersion.PHRASES.getString("QnVersion.intro"),
                Env.read("Rultor-Version"),
                Env.read("Rultor-Revision")
            )
        );
        Logger.info(this, "version request in #%d", comment.issue().number());
        return Req.DONE;
    }

}
