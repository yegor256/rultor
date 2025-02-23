/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.Joined;

/**
 * Question asked by one of them.
 *
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "xpath", "origin" })
public final class QnAskedBy implements Question {

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
    public QnAskedBy(final Profile prof, final String path,
        final Question qtn) {
        this.profile = prof;
        this.xpath = path;
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final Collection<String> logins = this.commanders(
            comment.issue().repo()
        );
        if (logins.isEmpty() || logins.contains(comment.author().login())) {
            req = this.origin.understand(comment, home);
        } else {
            new Answer(comment).post(
                false,
                String.format(
                    QnAskedBy.PHRASES.getString("QnAskedBy.denied"),
                    this.commandersAsDelimitedList(
                        logins,
                        comment.issue().repo().github().users().self().login()
                    )
                )
            );
            req = Req.EMPTY;
        }
        return req;
    }

    /**
     * Format list of commanders with {@code @} prefix, comma-delimited.
     * @param logins Commanders
     * @param excluded Excluded commander
     * @return Comma-delimited names
     * @checkstyle NonStaticMethodCheck (10 lines)
     */
    private String commandersAsDelimitedList(final Collection<String> logins,
        final String excluded) {
        return new Joined(
            ", ",
            new Mapped<>(
                input -> String.format("@%s", input),
                new Filtered<>(
                    login -> !excluded.equals(login),
                    logins
                )
            )
        ).toString();
    }

    /**
     * Get list of commanders.
     * @param repo Repo
     * @return Their logins
     * @throws IOException If fails
     */
    private Collection<String> commanders(final Repo repo) throws IOException {
        final Collection<String> logins = new LinkedList<>();
        final XML xml = this.profile.read();
        logins.addAll(new Crew(repo).names());
        logins.addAll(xml.xpath(this.xpath));
        logins.addAll(xml.xpath("/p/entry[@key='architect']/item/text()"));
        return logins;
    }

}
