/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Comment.Smart;
import com.jcabi.github.Issue;
import com.jcabi.github.Smarts;
import com.jcabi.github.safe.SfComments;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Reversed;
import org.cactoos.list.ListOf;
import org.cactoos.text.Abbreviated;
import org.cactoos.text.FormattedText;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;
import org.xembly.Xembler;

/**
 * Answer to post.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "comment")
public final class Answer {

    /**
     * Maximum messages from me.
     */
    private static final int MAX = 5;

    /**
     * Space char.
     */
    private static final String SPACE = " ";

    /**
     * Original comment.
     */
    private final transient Comment.Smart comment;

    /**
     * Ctor.
     * @param cmt Comment
     */
    public Answer(final Comment.Smart cmt) {
        this.comment = cmt;
    }

    /**
     * Post it..
     * @param success Is it a report about success?
     * @param msg Message
     * @param args Arguments
     * @throws IOException If fails
     */
    public void post(final boolean success, final String msg,
        final Object... args) throws IOException {
        final Issue issue = this.comment.issue();
        final List<Smart> comments = new ListOf<>(
            new Reversed<>(
                new Smarts<>(
                    issue.comments().iterate(new Date(0L))
                )
            )
        );
        final String self = issue.repo().github().users().self().login();
        int mine = 0;
        for (final Comment.Smart cmt : comments) {
            if (!cmt.author().login().equals(self)) {
                break;
            }
            ++mine;
        }
        if (mine < Answer.MAX) {
            new SfComments(
                issue.comments()
            ).post(this.msg(success, Logger.format(msg, args)));
        } else {
            Logger.error(
                this, "too many (%d) comments from %s already in %s#%d",
                mine, self, issue.repo().coordinates(), issue.number()
            );
        }
    }

    /**
     * Make a message to post.
     * @param success Is it a report about success?
     * @param text The text
     * @return Text to post
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private String msg(final boolean success, final String text) {
        final StringBuilder msg = new StringBuilder(100);
        try {
            msg.append(
                new FormattedText(
                    "> %s\n\n",
                    new Abbreviated(
                        this.comment.body().replaceAll(
                            "\\p{Space}",
                            Answer.SPACE
                        ),
                        100
                    )
                ).asString()
            );
            final Collection<String> logins = new TreeSet<>();
            logins.add(this.comment.author().login());
            if (!success) {
                logins.add(
                    new Issue.Smart(this.comment.issue()).author().login()
                );
            }
            msg.append(
                new UncheckedText(
                    new Joined(
                        Answer.SPACE,
                        new Mapped<>(
                            login -> String.format(
                                "@%s", login.toLowerCase(Locale.ENGLISH)
                            ),
                            logins
                        )
                    )
                ).asString()
            );
            msg.append(' ').append(text);
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            msg.append(text);
        }
        return Xembler.escape(msg.toString());
    }

}
