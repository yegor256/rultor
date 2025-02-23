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
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;
import org.xembly.Directives;

/**
 * Test case for {@link TkSitemap}.
 * @since 1.50
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class TkSitemapTest {

    /**
     * TkSitemap can render a list.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersListOfTalks() throws Exception {
        final Talks talks = new Talks.InDir();
        final Take take = new TkSitemap(talks);
        talks.create("repo1", Talk.TEST_NAME);
        talks.get(Talk.TEST_NAME).modify(
            new Directives()
                .xpath("/talk").push().xpath("wire").remove().pop()
                .add("wire").add("href").set("http://example.com").up()
                .add("github-repo").set("yegor256/rultor").up()
                .add("github-issue").set("555").up().up()
                .add("archive").add("log").attr("title", "hello, world")
                .attr("id", "a1b2c3").set("s3://test")
        );
        MatcherAssert.assertThat(
            "Sitemap should be generated",
            XhtmlMatchers.xhtml(
                new TextOf(
                    new RsPrint(take.act(new RqFake())).body()
                ).asString()
            ),
            XhtmlMatchers.hasXPath(
                "/ns1:urlset[count(ns1:url)=1]",
                "http://www.sitemaps.org/schemas/sitemap/0.9"
            )
        );
    }

}
