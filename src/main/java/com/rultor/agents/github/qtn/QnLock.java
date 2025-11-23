/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Contents;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import jakarta.json.Json;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.text.Joined;
import org.cactoos.text.Replaced;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Lock branch.
 *
 * @since 1.53
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnLock implements Question {

    /**
     * Message bundle.
     */
    private static final String PATH = ".rultor.lock";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final XML args = QnLock.args(comment, home);
        final String branch;
        if (args.nodes("//arg[@name='branch']").isEmpty()) {
            branch = new DefaultBranch(comment.issue().repo()).toString();
        } else {
            branch = args.xpath("//arg[@name='branch']/text()").get(0);
        }
        final Collection<String> users = new LinkedHashSet<>(0);
        users.add(comment.author().login().toLowerCase(Locale.ENGLISH));
        if (!args.nodes("//arg[@name='users']").isEmpty()) {
            users.addAll(
                new ListOf<>(
                    new Mapped<>(
                        input -> new Replaced(
                            new TextOf(input.trim().toLowerCase(Locale.ENGLISH)),
                            "^@", ""
                        ).asString(),
                        Arrays.asList(
                            args.xpath(
                                "//arg[@name='users']/text()"
                            ).get(0).split(",")
                        )
                    )
                )
            );
        }
        final Contents contents = comment.issue().repo().contents();
        if (contents.exists(QnLock.PATH, branch)) {
            new Answer(comment).post(
                false,
                String.format(
                    QnLock.PHRASES.getString("QnLock.already-exists"),
                    branch
                )
            );
        } else {
            contents.create(
                Json.createObjectBuilder()
                    .add("path", QnLock.PATH)
                    .add(
                        "message",
                        String.format(
                            "#%d: branch \"%s\" locked by request of @%s",
                            comment.issue().number(),
                            branch,
                            comment.author().login()
                        )
                    )
                    .add(
                        "content",
                        Base64.encodeBase64String(
                            new UncheckedText(
                                new Joined(
                                    "\n",
                                    users
                                )
                            ).asString().getBytes(StandardCharsets.UTF_8)
                        )
                    )
                    .add("branch", branch)
                    .build()
            );
            new Answer(comment).post(
                true,
                String.format(
                    QnLock.PHRASES.getString("QnLock.response"),
                    branch,
                    new UncheckedText(
                        new Joined(
                            ", ",
                            new Mapped<>(
                                input -> String.format("@%s", input),
                                users
                            )
                        )
                    ).asString()
                )
            );
        }
        Logger.info(this, "lock request in #%d", comment.issue().number());
        return Req.DONE;
    }

    /**
     * Get args.
     * @param comment The comment
     * @param home Home
     * @return Args
     * @throws IOException If fails
     */
    private static XML args(final Comment.Smart comment, final URI home)
        throws IOException {
        return new XMLDocument(
            new Xembler(
                new Directives().add("args").up().append(
                    new QnParametrized(
                        (cmt, hme) -> () -> new Directives().xpath("/")
                    ).understand(comment, home).dirs()
                )
            ).xmlQuietly()
        );
    }

}
