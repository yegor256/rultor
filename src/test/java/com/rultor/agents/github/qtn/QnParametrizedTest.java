/**
 * Copyright (c) 2009-2018, rultor.com
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
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnParametrized}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3.6
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class QnParametrizedTest {

    /**
     * QnParametrized can fetch params.
     * @throws Exception In case of error.
     */
    @Test
    public void fetchesParams() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hey, tag=`1.9` and server is `p5`");
        final Question origin = new Question() {
            @Override
            public Req understand(final Comment.Smart comment, final URI home) {
                return new Req() {
                    @Override
                    public Iterable<Directive> dirs() {
                        return new Directives()
                            .add("args").add("arg").set("hello, all").up().up()
                            .add("type").set("xxx").up();
                    }
                };
            }
        };
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("request").append(
                    new QnParametrized(origin).understand(
                        new Comment.Smart(issue.comments().get(1)), new URI("#")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/request[type='xxx']",
                "/request/args[count(arg) = 3]",
                "/request/args/arg[@name='tag' and .='1.9']",
                "/request/args/arg[@name='server' and .='p5']"
            )
        );
    }

    /**
     * QnParametrized can ignore if there are no params.
     * @throws Exception In case of error.
     */
    @Test
    public void ignoresEmptyParams() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hey");
        MatcherAssert.assertThat(
            new QnParametrized(Question.EMPTY).understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#1")
            ).dirs(),
            Matchers.emptyIterable()
        );
    }

    /**
     * QnParametrized can ignore empty request.
     * @throws Exception In case of error.
     */
    @Test
    public void ignoresEmptyReq() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hey you");
        MatcherAssert.assertThat(
            new QnParametrized(Question.EMPTY).understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#2")
            ),
            Matchers.is(Req.EMPTY)
        );
    }

    /**
     * QnParametrized can ignore LATER request.
     * @throws Exception In case of error.
     */
    @Test
    public void ignoresLaterReq() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("");
        final Question question = new Question() {
            @Override
            public Req understand(final Comment.Smart comment, final URI home) {
                return Req.LATER;
            }
        };
        MatcherAssert.assertThat(
            new QnParametrized(question).understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#2")
            ),
            Matchers.is(Req.LATER)
        );
    }

}
