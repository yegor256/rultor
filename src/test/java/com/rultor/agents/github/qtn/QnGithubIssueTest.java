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
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.spi.Talk;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnGithubIssue}.
 *
 * @since 2.0
 */
final class QnGithubIssueTest {

    /**
     * QnGithubIssue can pass github_issue as env variable.
     * @throws Exception In case of error.
     */
    @Test
    void canAddGithubIssueVariable() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("test comment.");
        final Question origin = (comment, home) ->
            (Req) () -> new Directives().add("type").set("xxx").up();
        MatcherAssert.assertThat(
            "request should save issue data",
            new StrictXML(
                new XMLDocument(
                    new Xembler(
                        new Directives().add("talk")
                            .attr("name", "abc")
                            .attr("later", "false")
                            .attr("number", "123")
                            .add("request")
                            .attr("id", "a1b2c3")
                            .add("author").set("yegor256").up()
                            .append(
                                new QnGithubIssue(origin).understand(
                                    new Comment.Smart(
                                        issue.comments().get(1)
                                    ),
                                    new URI("#")
                                ).dirs()
                            )
                            .addIf("args")
                    ).xml()
                ),
                Talk.SCHEMA
            ),
            XhtmlMatchers.hasXPaths(
                "/talk/request[type='xxx']",
                "/talk/request/args[count(arg) = 1]",
                String.format(
                    "/talk/request/args/arg[@name='github_issue' and .='%d']",
                    issue.number()
                )
            )
        );
    }
}
