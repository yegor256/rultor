/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.mock.MkGitHub;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link QnSafe}.
 *
 * @since 1.76
 */
final class QnSafeTest {

    /**
     * QnSafe can understand a comment even if some exception is thrown.
     * It's worth noting that the exception text has to be included after ```
     * and EMPTY LINE, otherwise, the first line of the exception message
     * will be treated as language specification and invisible in GitHub
     * message.
     * Compare
     * <p>{@code
     *     ```java.lang.IllegalArgumentException: Illegal argument exception
     * }</p>
     * with
     * <p>{@code
     *     ```
     *     java.lang.IllegalArgumentException: Illegal argument exception
     * }</p>
     * The first example will print empty message to GitHub comment because it
     * will be treated as unknown language:
     * 'java.lang.IllegalArgumentException: Illegal argument exception'
     *
     * @throws URISyntaxException if URI is invalid
     * @throws IOException if I/O fails.
     */
    @Test
    void understandsWithThrowable()
        throws URISyntaxException, IOException {
        final Issue issue = new MkGitHub().randomRepo()
            .issues()
            .create("", "");
        final Comment post = issue.comments().post("Hello, world!");
        new QnSafe(
            (comment, home) -> {
                throw new IllegalArgumentException(
                    "Illegal argument exception",
                    new IOException("Artificial cause")
                );
            }
        )
            .understand(
                new Comment.Smart(post),
                new URI("http://www.example.com")
        );
        MatcherAssert.assertThat(
            "Two comments should be posted",
            issue.comments().iterate(new Date(0)),
            Matchers.iterableWithSize(2)
        );
        final String body = new Comment.Smart(issue.comments().get(2)).body();
        MatcherAssert.assertThat(
            "Failed comment should be posted with part of the log",
            body, Matchers.allOf(
                Matchers.containsString("We failed, sorry"),
                Matchers.containsString("```\n")
            )
        );
    }
}
