/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.takes.Take;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.fork.RqRegex;
import org.takes.rq.RqFake;
import org.xembly.Directives;

/**
 * Test case for {@link TkDaemon}.
 * @since 1.50
 */
final class TkDaemonTest {

    /**
     * TkDaemon can show log in HTML.
     * @throws Exception If some problem inside
     */
    @Test
    void showsLogInHtml() throws Exception {
        final Talks talks = new Talks.InDir();
        final String name = "test";
        talks.create(name, Talk.TEST_NAME);
        final Talk talk = talks.get(name);
        final File tail = File.createTempFile(
            TkDaemonTest.class.getCanonicalName(), ".txt"
        );
        final String content = "1 < привет > тебе от меня";
        FileUtils.writeStringToFile(tail, content, StandardCharsets.UTF_8);
        talk.modify(
            new Directives().xpath("/talk").add("daemon")
                .attr("id", "00000000")
                .add("dir").set(tail.getAbsolutePath()).up()
                .add("script").set("no script").up()
                .add("title").set("no title")
        );
        final Take take = new TkAuth(
            request -> new TkDaemon(talks).act(
                new RqRegex.Fake("(.*)-(.*)", "1-abcd")
            ),
            new PsFake(true)
        );
        MatcherAssert.assertThat(
            "Talk answer should contain data from tail",
            XhtmlMatchers.xhtml(
                IOUtils.toString(
                    take.act(new RqFake()).body(),
                    StandardCharsets.UTF_8
                )
            ),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html/xhtml:body",
                "//xhtml:a[@href='https://github.com/test']",
                String.format("//xhtml:pre[contains(., '%s')]", content)
            )
        );
    }

}
