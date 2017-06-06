/**
 * Copyright (c) 2009-2016, rultor.com
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
import com.jcabi.github.Comments;
import com.jcabi.github.Event;
import com.jcabi.github.Issue;
import com.jcabi.github.IssueLabels;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.ResourceBundle;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.validation.constraints.NotNull;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for ${@link QnIfPull}.
 *
 * @author Denys Skalenko (d.skalenko@gmail.com)
 * @version $Id$
 * @since 2.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class QnIfPullTest {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * QnIfPull can allow a request.
     * @throws Exception In case of error.
     */
    @Test
    public void allowsRequest() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().get(
            repo.pulls().create("", "head", "base").number()
        );
        issue.comments().post("merge");
        MatcherAssert.assertThat(
            new QnIfPull(new QnHello()).understand(
                new Comment.Smart(
                    new QnIfPullTest.MockComment(
                        issue.comments().get(1)
                    )
                ), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                QnIfPullTest.PHRASES.getString("QnHello.intro")
            )
        );
    }

    /**
     * QnIfPull can block a not pull request.
     * @throws Exception In case of error.
     */
    @Test
    public void blocksNotPullRequest() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().get(
            repo.pulls().create("", "head", "base").number()
        );
        issue.comments().post("merge");
        final Comment.Smart comment =
            new Comment.Smart(issue.comments().get(1));
        new Issue.Smart(comment.issue()).close();
        MatcherAssert.assertThat(
            new QnIfPull(new QnHello()).understand(
                comment, new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                QnIfPullTest.PHRASES.getString("QnIfPull.not-pull-request")
            )
        );
    }

    /**
     * QnIfPull can block a closed request.
     * @throws Exception In case of error.
     */
    @Test
    public void blocksClosedRequest() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().get(
            repo.pulls().create("", "head", "base").number()
        );
        issue.comments().post("merge");
        final Comment.Smart comment =
            new Comment.Smart(
                new QnIfPullTest.MockComment(issue.comments().get(1))
            );
        new Issue.Smart(comment.issue()).close();
        MatcherAssert.assertThat(
            new QnIfPull(new QnHello()).understand(
                comment, new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString(
                QnIfPullTest.PHRASES.getString("QnIfPull.already-closed")
            )
        );
    }

    /**
     * Mock comment.
     */
    private static final class MockComment implements Comment {

        /**
         * Origin comment.
         */
        private final transient Comment origin;

        /**
         * Ctor.
         * @param comment Origin comment
         */
        private MockComment(final Comment comment) {
            this.origin = comment;
        }

        @Override
        public Issue issue() {
            return new QnIfPullTest.MockIssue(this.origin.issue());
        }

        @Override
        public int number() {
            return this.origin.number();
        }

        @Override
        public void remove() throws IOException {
            this.origin.remove();
        }

        @Override
        public int compareTo(final Comment comment) {
            return this.origin.compareTo(comment);
        }

        @Override
        public void patch(@NotNull(message = "JSON is never NULL")
            final JsonObject json) throws IOException {
            this.origin.patch(json);
        }

        @Override
        public JsonObject json() throws IOException {
            return this.origin.json();
        }
    }

    /**
     * Mock issue.
     */
    private static final class MockIssue implements Issue {

        /**
         * Origin issue.
         */
        private final transient Issue origin;

        /**
         * Ctor.
         * @param issue Origin issue
         */
        MockIssue(final Issue issue) {
            this.origin = issue;
        }

        @Override
        public JsonObject json() throws IOException {
            final JsonObjectBuilder json = Json.createObjectBuilder();
            for (final Map.Entry<String, JsonValue> val
                : this.origin.json().entrySet()) {
                json.add(val.getKey(), val.getValue());
            }
            return json
                .add(
                    "pull_request",
                    Json.createObjectBuilder()
                        .add("html_url", "http://test2").build()
                )
                .build();
        }

        @Override
        public Repo repo() {
            return this.origin.repo();
        }

        @Override
        public int number() {
            return this.origin.number();
        }

        @Override
        public Comments comments() {
            return this.origin.comments();
        }

        @Override
        public IssueLabels labels() {
            return this.origin.labels();
        }

        @Override
        public Iterable<Event> events() throws IOException {
            return this.origin.events();
        }

        @Override
        public boolean exists() throws IOException {
            return this.origin.exists();
        }

        @Override
        public int compareTo(final Issue issue) {
            return this.origin.compareTo(issue);
        }

        @Override
        public void patch(@NotNull(message = "JSON is never NULL")
            final JsonObject json) throws IOException {
            this.origin.patch(json);
        }
    }
}
