/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
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
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;

/**
 * Release request.
 *
 * @since 1.3.6
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnRelease implements Question {

    /**
     * Pattern matching the version tag of the release enclosed by backticks.
     */
    private static final Pattern QUESTION_PATTERN = Pattern.compile("`(.+)`");

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Issue issue = comment.issue();
        Logger.info(
            this, "release request found in %s#%d, comment #%d",
            issue.repo().coordinates(), issue.number(), comment.number()
        );
        final Req req;
        final Matcher matcher = QnRelease.QUESTION_PATTERN
            .matcher(comment.body());
        if (matcher.find()) {
            final String name = matcher.group(1);
            final ReleaseTag release = new ReleaseTag(issue.repo(), name);
            if (release.allowed()) {
                req = QnRelease.affirmative(comment, home);
            } else {
                new Answer(comment).post(
                    false,
                    String.format(
                        QnRelease.PHRASES.getString("QnRelease.invalid-tag"),
                        name,
                        release.reference()
                    )
                );
                req = Req.EMPTY;
            }
        } else {
            req = QnRelease.affirmative(comment, home);
        }
        return req;
    }

    /**
     * Confirms that Rultor is starting the release process.
     * @param comment Comment that triggered the release
     * @param home URI of the release tail
     * @return Req.Simple containing the release parameters
     * @throws IOException on error
     */
    private static Req affirmative(final Comment.Smart comment,
        final URI home) throws IOException {
        new Answer(comment).post(
            true,
            String.format(
                QnRelease.PHRASES.getString("QnRelease.start"),
                home.toASCIIString()
            )
        );
        return new Req.Simple(
            "release",
            new MapOf<String, String>(
                new MapEntry<>(
                    "head_branch",
                    new DefaultBranch(comment.issue().repo()).toString()
                ),
                new MapEntry<>(
                    "head",
                    String.format(
                        "git@github.com:%s.git",
                        comment.issue().repo().coordinates()
                    )
                )
            )
        );
    }

}
