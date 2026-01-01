/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Show current config.
 *
 * @since 1.8
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "profile")
public final class QnConfig implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param prof Profile
     */
    public QnConfig(final Profile prof) {
        this.profile = prof;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        new Answer(comment).post(
            true,
            String.format(
                QnConfig.PHRASES.getString("QnConfig.response"),
                this.profile.read().toString()
            )
        );
        Logger.info(this, "config request in #%d", comment.issue().number());
        return Req.DONE;
    }

}
