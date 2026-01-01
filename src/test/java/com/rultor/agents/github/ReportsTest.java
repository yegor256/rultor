/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.log.Logger;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.ResourceBundle;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link Reports}.
 *
 * @since 1.3
 */
final class ReportsTest {
    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES = ResourceBundle.getBundle(
        "phrases"
    );

    /**
     * Duration of command execution.
     */
    private static final long DURATION = 1_234_567;

    /**
     * Xpath to check that talk was executed correctly.
     */
    private static final String XPATH = "/talk[not(request)]";

    /**
     * Reports can report a result of a request.
     * @throws Exception In case of error
     */
    @Test
    void reportsRequestResult() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Talk talk = ReportsTest.example(
            repo, repo.issues().create("", "")
        );
        final Agent agent = new Reports(repo.github());
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Request should not be created",
            talk.read(),
            XhtmlMatchers.hasXPath(ReportsTest.XPATH)
        );
    }

    /**
     * Reports can report a result of a request, when stop command fails.
     * @throws Exception In case of error
     */
    @Test
    void reportsRequestResultWhenStopFails() throws Exception {
        final String user = "john";
        final String stop = "stop it please";
        final Repo repo = new MkGitHub(user).randomRepo();
        final Talk talk = ReportsTest.example(
            repo, repo.issues().create("Bug", stop)
        );
        final Agent agent = new Reports(repo.github());
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Request should not be created",
            talk.read(),
            XhtmlMatchers.hasXPath(ReportsTest.XPATH)
        );
        MatcherAssert.assertThat(
            "Comment contains warning about stop request",
            repo.issues().get(1).comments().get(1).json().getString(
                "body"
            ),
            Matchers.is(
                String.format(
                    "> %s\n\n@%s %s %s",
                    stop,
                    user,
                    ReportsTest.PHRASES.getString("Reports.stop-fails"),
                    Logger.format(
                        ReportsTest.PHRASES.getString("Reports.success"),
                        "https://www.rultor.com/t/1-1",
                        ReportsTest.DURATION
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
                .add("msec").set(ReportsTest.DURATION).up()
                .add("success").set("true").up()
                .add("type").set("something").up()
                .add("args")
        );
        return result;
    }
}
