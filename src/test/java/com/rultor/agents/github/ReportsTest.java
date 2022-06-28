/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor.agents.github;

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.log.Logger;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.ResourceBundle;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link Reports}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3
 */
public final class ReportsTest {
    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES = ResourceBundle.getBundle(
        "phrases"
    );

    /**
     * Duration of command execution.
     */
    private static final long DURATION = 1234567;

    /**
     * Xpath to check that talk was executed correctly.
     */
    private static final String XPATH = "/talk[not(request)]";

    /**
     * Reports can report a result of a request.
     * @throws Exception In case of error
     */
    @Test
    public void reportsRequestResult() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Talk talk = ReportsTest.example(
            repo, repo.issues().create("", "")
        );
        final Agent agent = new Reports(repo.github());
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath(XPATH)
        );
    }

    /**
     * Reports can report a result of a request, when stop command fails.
     * @throws Exception In case of error
     */
    @Test
    public void reportsRequestResultWhenStopFails() throws Exception {
        final String user = "john";
        final String stop = "stop it please";
        final Repo repo = new MkGithub(user).randomRepo();
        final Talk talk = ReportsTest.example(
            repo, repo.issues().create("Bug", stop)
        );
        final Agent agent = new Reports(repo.github());
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath(XPATH)
        );
        MatcherAssert.assertThat(
            "Comment contains warning about stop request",
            repo.issues().get(1).comments().get(1).json().getString(
                "body"
            ).equals(
                String.format(
                    "> %s\n\n@%s %s %s",
                    stop,
                    user,
                    ReportsTest.PHRASES.getString("Reports.stop-fails"),
                    Logger.format(
                        ReportsTest.PHRASES.getString("Reports.success"),
                        "https://www.rultor.com/t/1-1",
                        DURATION
                    )
                )
            )
        );
    }

    /**
     * Create Talk, that will be used to test Reports.
     * @param repo Repository
     * @param issue Issue
     * @return Example of Talk
     * @throws IOException In case of error
     */
    private static Talk example(final Repo repo, final Issue issue)
        throws IOException {
        final Talk result = new Talk.InFile();
        result.modify(
            new Directives().xpath("/talk").add("wire")
                .add("href").set("http://test").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .up()
                .add("request").attr("id", "1")
                .add("author").set("yegor256").up()
                .add("msec").set(DURATION).up()
                .add("success").set("true").up()
                .add("type").set("something").up()
                .add("args")
        );
        return result;
    }
}
