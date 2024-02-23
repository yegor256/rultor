/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnReferredTo}.
 *
 * @since 1.20.3
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class QnReferredToTest {

    /**
     * QnReferredTo can build a request.
     * @throws Exception In case of error.
     */
    @Test
    void buildsRequest() throws Exception {
        MatcherAssert.assertThat(
            this.xemblerXml("  @xx deploy"),
            XhtmlMatchers.hasXPath("/request/type")
        );
        MatcherAssert.assertThat(
            this.xemblerXml("  @xx, deploy"),
            XhtmlMatchers.hasXPath("/request/type")
        );
    }

    /**
     * QnReferredTo can recognize mentions delimited by a comma.
     * @throws Exception In case of error.
     */
    @Test
    void recognizesCommaAsDelimiter() throws Exception {
        final String login = "xx";
        MatcherAssert.assertThat(
            this.reqFromComment(
                String.format("hello @%s, deploy", login), login
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            this.reqFromComment(
                String.format("hello ,@%s deploy", login), login
            ),
            Matchers.is(Req.DONE)
        );
    }

    /**
     * QnReferredTo can recognize mention as invalid when login is
     * bounded by non-boundary char.
     * @throws Exception In case of error.
     */
    @Test
    void recognizesInvalidBoundary() throws Exception {
        final String login = "xx";
        MatcherAssert.assertThat(
            this.reqFromComment(
                String.format("hello @%sx deploy", login), login
            ),
            Matchers.is(Req.EMPTY)
        );
        MatcherAssert.assertThat(
            this.reqFromComment(
                String.format("hello x@%s deploy", login), login
            ),
            Matchers.is(Req.EMPTY)
        );
    }

    /**
     * QnReferredTo can answer when mention.
     * @throws Exception In case of error.
     */
    @Test
    void answerWhenMentioned() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final String login = "xx";
        issue.comments().post(String.format("hello @%s deploy", login));
        MatcherAssert.assertThat(
            new QnReferredTo(login, new QnDeploy()).understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                new Joined(
                    "",
                    "I see you're talking about me, but I don't",
                    " understand it. If you want to say something to me",
                    " directly, start a message with @",
                    login
                ).asString()
            )
        );
    }

    /**
     * Return the Req for a comment.
     *
     * @param comment String comment to read
     * @param login String Rultor Github user login
     * @return Req
     * @throws Exception In case of error.
     */
    private Req reqFromComment(final String comment, final String login)
        throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post(comment);
        return new QnReferredTo(login, new QnDeploy()).understand(
            new Comment.Smart(issue.comments().get(1)), new URI("#")
        );
    }

    /**
     * Return the Xembler xml output for a comment.
     *
     * @param comment String comment to read
     * @return String
     * @throws Exception In case of error.
     */
    private String xemblerXml(final String comment) throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post(comment);
        return new Xembler(
            new Directives().add("request").append(
                new QnReferredTo("xx", new QnDeploy()).understand(
                    new Comment.Smart(issue.comments().get(1)), new URI("#")
                ).dirs()
            )
        ).xml();
    }
}
