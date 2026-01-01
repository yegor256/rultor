/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Contents;
import com.jcabi.github.Issue;
import com.jcabi.github.Pull;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;

/**
 * If target branch is unlocked.
 *
 * @since 1.53
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnIfUnlocked implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Message bundle.
     */
    private static final String PATH = ".rultor.lock";

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnIfUnlocked(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final Issue issue = comment.issue();
        final Pull pull = issue.repo().pulls().get(issue.number());
        final JsonObject base = pull.json().getJsonObject("base");
        final String branch = base.getString("ref");
        final Collection<String> guards = QnIfUnlocked.guards(pull, branch);
        if (guards.isEmpty() || guards.contains(comment.author().login())) {
            req = this.origin.understand(comment, home);
        } else {
            new Answer(comment).post(
                false,
                QnIfUnlocked.PHRASES.getString("QnIfUnlocked.denied"),
                branch,
                new UncheckedText(
                    new Joined(
                        ", ",
                        new Mapped<>(
                            input -> String.format("@%s", input),
                            guards
                        )
                    )
                ).asString()
            );
            req = Req.EMPTY;
        }
        return req;
    }

    /**
     * Is it allowed to merge?
     * @param pull The pull
     * @param branch The branch
     * @return TRUE if allowed
     * @throws IOException If fails
     */
    private static Collection<String> guards(final Pull pull,
        final String branch) throws IOException {
        final Contents contents = pull.repo().contents();
        final Collection<String> guards = new LinkedHashSet<>(0);
        if (contents.exists(QnIfUnlocked.PATH, branch)) {
            guards.addAll(
                Arrays.asList(
                    IOUtils.toString(
                        contents.get(QnIfUnlocked.PATH, branch).raw(),
                        StandardCharsets.UTF_8
                    ).split("\n")
                )
            );
        }
        return guards;
    }

}
