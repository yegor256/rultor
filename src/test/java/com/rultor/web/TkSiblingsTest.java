/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rs.RsPrint;
import org.xembly.Directives;

/**
 * Test case for {@link TkSiblings}.
 * @since 1.23.1
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class TkSiblingsTest {

    /**
     * TkSiblings can render a list.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersListOfTalks() throws Exception {
        final Talks talks = new Talks.InDir();
        final TkRegex take = new TkSiblings(talks);
        talks.create("repo1", Talk.TEST_NAME);
        talks.get(Talk.TEST_NAME).modify(
            new Directives()
                .xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("http://example.com").up()
                .add("github-repo").set("yegor256/rultor").up()
                .add("github-issue").set("555").up().up()
                .add("archive").add("log").attr("title", "hello, world")
                .attr("id", "a1b2c3").set("s3://test")
        );
        MatcherAssert.assertThat(
            "Response on GET request should contain info",
            XhtmlMatchers.xhtml(
                new TextOf(
                    new RsPrint(
                        take.act(
                            new RqRegex.Fake(
                                new RqWithHeader(
                                    new RqFake("GET", "/aa?s=123"),
                                    "Accept", "text/xml"
                                ),
                                "(.*)",
                                "x"
                            )
                        )
                    ).body()
                ).asString()
            ),
            XhtmlMatchers.hasXPaths(
                "/page[repo='x']",
                "/page[since='123']",
                "/page/siblings[count(talk)=1]",
                "/page/siblings/talk[timeago]",
                "/page/siblings/talk/archive/log[id and href and title]",
                "/page/siblings/talk/archive[count(log)=1]",
                "//log[starts-with(href,'https://')]"
            )
        );
    }

}
