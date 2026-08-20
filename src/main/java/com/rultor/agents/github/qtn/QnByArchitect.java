/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;

/**
 * Question by architect only (if configured).
 * @since 1.45
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "xpath", "origin" })
public final class QnByArchitect implements Question {

    /**
     * Mention prefix in command.
     */
    private static final String MENTION = "^@[^\\s,:]+[\\s,:]*";

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
     * XPath.
     */
    private final transient String xpath;

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param prof Profile
     * @param path XPath in profile with a list of logins
     * @param qtn Original question
     */
    public QnByArchitect(final Profile prof, final String path,
        final Question qtn) {
        this.profile = prof;
        this.xpath = path;
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final List<String> logins = new ListOf<>(
            new Mapped<>(
                input -> input.toLowerCase(Locale.ENGLISH),
                this.profile.read().xpath(this.xpath)
            )
        );
        final String author = comment.author()
            .login()
            .toLowerCase(Locale.ENGLISH);
        if (
            logins.contains(author)
                || QnByArchitect.isMergeByArchitectPull(comment, logins)
        ) {
            req = this.origin.understand(comment, home);
        } else if (logins.isEmpty()) {
            if (QnByArchitect.allowed(comment.issue().repo(), author)) {
                req = this.origin.understand(comment, home);
            } else {
                new Answer(comment).post(
                    true,
                    QnByArchitect.PHRASES.getString(
                        "QnByArchitect.read-only"
                    )
                );
                req = Req.DONE;
            }
        } else {
            new Answer(comment).post(
                true,
                String.format(
                    QnByArchitect.PHRASES.getString("QnByArchitect.denied"),
                    logins.get(0).toLowerCase(Locale.ENGLISH)
                )
            );
            req = Req.DONE;
        }
        return req;
    }

    private static boolean isMergeByArchitectPull(final Comment.Smart comment,
        final List<String> logins) throws IOException {
        final Issue issue = comment.issue();
        final Issue.Smart smart;
        if (issue instanceof Issue.Smart) {
            smart = (Issue.Smart) issue;
        } else {
            smart = new Issue.Smart(issue);
        }
        return !logins.isEmpty()
            && smart.isPull()
            && logins.contains(
                smart.author().login().toLowerCase(Locale.ENGLISH)
            )
            && QnByArchitect.isMerge(comment.body());
    }

    private static boolean isMerge(final String body) {
        final String text = body.trim().toLowerCase(Locale.ENGLISH);
        return text.startsWith("merge")
            || text.replaceFirst(QnByArchitect.MENTION, "")
                .trim()
                .startsWith("merge");
    }

    private static boolean allowed(final Repo repo,
        final String author) throws IOException {
        String perm;
        try {
            perm = repo.collaborators().permission(author);
        } catch (final AssertionError ex) {
            perm = "forbidden";
        }
        return "write".equals(perm) || "admin".equals(perm);
    }
}
