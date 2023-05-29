/**
 * Copyright (c) 2009-2023 Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.mock.MkGithub;
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
 * @author Volodya Lombrozo (volodya.lombrozo@gmail.com)
 * @version $Id$
 * @since 1.76
 */
class QnSafeTest {

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
    public void understandsWithThrowable()
        throws URISyntaxException, IOException {
        final Issue issue = new MkGithub().randomRepo()
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
        ).understand(
            new Comment.Smart(post),
            new URI("http://www.example.com")
        );
        MatcherAssert.assertThat(
            issue.comments().iterate(new Date(0)),
            Matchers.iterableWithSize(2)
        );
        final String body = new Comment.Smart(issue.comments().get(2)).body();
        MatcherAssert.assertThat(
            body, Matchers.allOf(
                Matchers.containsString("We failed, sorry"),
                Matchers.containsString("```\n")
            )
        );
    }
}
