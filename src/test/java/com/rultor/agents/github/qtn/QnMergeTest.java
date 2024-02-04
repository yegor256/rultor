/**
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

import com.jcabi.github.Check;
import com.jcabi.github.Comment;
import com.jcabi.github.Comments;
import com.jcabi.github.Pull;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkChecks;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.matchers.XhtmlMatchers;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnMerge}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.6
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class QnMergeTest {

    /**
     * The default command to the rultor with a request to merge changes.
     */
    private static final String COMMAND = "@rultor, merge, please";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * All pull request comments.
     */
    private transient Comments comments;

    /**
     * Pull request.
     */
    private transient Pull pull;

    /**
     * Initial phase for all tests.
     * @throws IOException In case of error.
     */
    @BeforeEach
    public void setUp() throws IOException {
        final Repo repo = new MkGithub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        final String head = "head";
        final String base = "base";
        branches.create(head, "abcdef4");
        branches.create(base, "abcdef5");
        this.pull = repo.pulls().create("", head, base);
        this.comments = repo.issues()
            .get(this.pull.number())
            .comments();
    }

    /**
     * QnMerge can build a request.
     *
     * @throws Exception In case of error
     */
    @Test
    public void buildsRequest() throws Exception {
        final String request = new Xembler(this.mergeRequest()).xml();
        MatcherAssert.assertThat(
            request,
            Matchers.allOf(
                XhtmlMatchers.hasXPath("/request/type[text()='merge']"),
                XhtmlMatchers.hasXPath(
                    "/request/args/arg[@name='fork_branch' and text()='head']"
                ),
                XhtmlMatchers.hasXPath(
                    "/request/args/arg[@name='head_branch' and text()='base']"
                )
            )
        );
        MatcherAssert.assertThat(
            new Comment.Smart(this.comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(this.comments.get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnMergeTest.PHRASES.getString("QnMerge.start"),
                    "#"
                )
            )
        );
    }

    /**
     * QnMerge can not build a request because some GitHub checks
     *  were failed.
     *
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    public void stopsBecauseCiChecksFailed()
        throws IOException, URISyntaxException {
        final MkChecks checks = (MkChecks) this.pull.checks();
        checks.create(Check.Status.IN_PROGRESS, Check.Conclusion.SUCCESS);
        this.mergeRequest();
        MatcherAssert.assertThat(
            new Comment.Smart(this.comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(this.comments.get(2)).body(),
            Matchers.containsString(
                QnMergeTest.PHRASES.getString("QnMerge.checks-are-failed")
            )
        );
    }

    /**
     * QnMerge can build a request because GitHub checks finished successfully.
     *
     * @throws IOException In case of I/O error
     * @throws URISyntaxException In case of URI error
     */
    @Test
    public void continuesBecauseCiChecksSuccessful()
        throws IOException, URISyntaxException {
        final MkChecks checks = (MkChecks) this.pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.SUCCESS);
        this.mergeRequest();
        MatcherAssert.assertThat(
            new Comment.Smart(this.comments.get(1)).body(),
            Matchers.is(QnMergeTest.COMMAND)
        );
        MatcherAssert.assertThat(
            new Comment.Smart(this.comments.get(2)).body(),
            Matchers.containsString(
                String.format(
                    QnMergeTest.PHRASES.getString("QnMerge.start"),
                    "#"
                )
            )
        );
    }

    /**
     * Merge request directives.
     * @return Directives
     * @throws IOException In case of error
     * @throws URISyntaxException In case of error
     */
    private Directives mergeRequest() throws IOException,
        URISyntaxException {
        return new Directives()
            .add("request")
            .append(
                new QnMerge().understand(
                    new Comment.Smart(
                        this.comments.post(QnMergeTest.COMMAND)
                    ),
                    new URI("#")
                ).dirs()
            );
    }
}
