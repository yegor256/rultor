/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
            "deploy request should be created",
            this.xemblerXml("  @xx deploy"),
            XhtmlMatchers.hasXPath("/request/type[text()='deploy']")
        );
        MatcherAssert.assertThat(
            "deploy request should be created",
            this.xemblerXml("  @xx, deploy"),
            XhtmlMatchers.hasXPath("/request/type[text()='deploy']")
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
            "deploy command should be recognized",
            this.reqFromComment(
                String.format("hello @%s, deploy", login), login
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "deploy command should be recognized",
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
            "Comment should be ignored without mention",
            this.reqFromComment(
                String.format("hello @%sx deploy", login), login
            ),
            Matchers.is(Req.EMPTY)
        );
        MatcherAssert.assertThat(
            "Comment should be ignored without mention",
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
            "Deploy command should be recognized, if mentioned",
            new QnReferredTo(login, new QnDeploy()).understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Answer comment should be posted with instruction",
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
