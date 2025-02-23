/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talks;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link IndexesRequests}.
 *
 * @since 1.2
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class IndexesRequestsTest {
    /**
     * IndexesRequests should store index when it doesn't exist.
     * @throws Exception In case of error.
     */
    @Test
    void storesIndexIfNone() throws Exception {
        final String name = "talk";
        final Talks talks = new Talks.InDir();
        talks.create("", name);
        talks.get(name).modify(
            new Directives()
                .xpath("/talk").push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#1").up().up()
                .add("request").attr("id", "a12345")
                .add("author").set("yegor256").up()
                .add("args").up()
                .add("type").set("deploy").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            "Talk should be reindexed (1 based)",
            talks.get(name).read(),
            XhtmlMatchers.hasXPaths("/talk/request[@index='1']")
        );
    }

    /**
     * IndexesRequests should retrieve index from log.
     * @throws Exception In case of error.
     */
    @Test
    void retrievesIndexFromLog() throws Exception {
        final String name = "talk";
        final Talks talks = new Talks.InDir();
        talks.create("", name);
        talks.get(name).modify(
            new Directives().xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#2").up().up()
                .add("archive")
                .add("log").attr("id", "1").attr("title", "title1")
                .attr("index", "1").up()
                .add("log").attr("id", "2").attr("title", "title2")
                .attr("index", "2").up().up()
                .add("request").attr("id", "a12345")
                .add("author").set("yegor256").up()
                .add("args").up()
                .add("type").set("deploy").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            "Index value should be started from log records max",
            talks.get(name).read(),
            XhtmlMatchers.hasXPaths("/talk/request[@index='3']")
        );
    }

    /**
     * IndexesRequests should retrieve index from sibling.
     * @throws Exception In case of error.
     */
    @Test
    void retrievesIndexFromSibling() throws Exception {
        final String first = "first";
        final Talks talks = new Talks.InDir();
        talks.create("", first);
        talks.get(first).modify(
            new Directives().xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#3").up().up()
                .add("archive")
                .add("log").attr("id", "3").attr("title", "title3")
                .attr("index", "1").up()
        );
        final String second = "second";
        talks.create("", second);
        talks.get(second).modify(
            new Directives().xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#4").up().up()
                .add("archive")
                .add("log").attr("id", "4").attr("title", "title4")
                .attr("index", "2").up()
        );
        final String third = "third";
        talks.create("", third);
        talks.get(third).modify(
            new Directives()
                .xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#5").up().up()
                .add("request").attr("id", "a67890")
                .add("author").set("yegor256").up()
                .add("args").up()
                .add("type").set("merge").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            "Index value should be started from log records max",
            talks.get(third).read(),
            XhtmlMatchers.hasXPaths("/talk/request[@index='5']")
        );
    }

    /**
     * IndexesRequests should not store index when request tag doesn't exist.
     * @throws Exception In case of error.
     */
    @Test
    void notStoreIndexWithoutRequest() throws Exception {
        final String name = "talk";
        final Talks talks = new Talks.InDir();
        talks.create("", name);
        talks.get(name).modify(
            new Directives()
                .xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#1").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            "Request tag should not be created if does not exist",
            talks.get(name).read(),
            Matchers.not(
                XhtmlMatchers.hasXPaths("/talk/request")
            )
        );
    }
}
