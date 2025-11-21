/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Content;
import com.jcabi.github.Contents;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import jakarta.json.Json;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Unlock branch.
 *
 * @since 1.53
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnUnlock implements Question {

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
        final XML args = QnUnlock.args(comment, home);
        final String branch;
        if (args.nodes("//arg[@name='branch']").isEmpty()) {
            branch = new DefaultBranch(comment.issue().repo()).toString();
        } else {
            branch = args.xpath("//arg[@name='branch']/text()").get(0);
        }
        final Contents contents = comment.issue().repo().contents();
        if (contents.exists(QnUnlock.PATH, branch)) {
            contents.remove(
                Json.createObjectBuilder()
                    .add("path", QnUnlock.PATH)
                    .add(
                        "sha",
                        new Content.Smart(
                            contents.get(QnUnlock.PATH, branch)
                        ).sha()
                    )
                    .add(
                        "message",
                        String.format(
                            "#%d branch \"%s\" unlocked, by request of @%s",
                            comment.issue().number(),
                            branch,
                            comment.author().login()
                        )
                    )
                    .add("branch", branch)
                    .build()
            );
            new Answer(comment).post(
                true,
                String.format(
                    QnUnlock.PHRASES.getString("QnUnlock.response"),
                    branch
                )
            );
        } else {
            new Answer(comment).post(
                false,
                String.format(
                    QnUnlock.PHRASES.getString("QnUnlock.does-not-exist"),
                    branch
                )
            );
        }
        Logger.info(this, "unlock request in #%d", comment.issue().number());
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
