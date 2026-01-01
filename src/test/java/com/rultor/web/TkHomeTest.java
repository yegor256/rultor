/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.Toggles;
import com.rultor.spi.Talks;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkHome}.
 * @since 1.50
 */
final class TkHomeTest {

    /**
     * TkHome can render home page.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersHomePage() throws Exception {
        final Talks talks = new Talks.InDir();
        final Take take = new TkHome(talks, new Toggles.InFile());
        talks.create("repo1", "test1");
        talks.create("repo2", "test2");
        MatcherAssert.assertThat(
            "Homepage should contain some data",
            XhtmlMatchers.xhtml(
                new TextOf(
                    new RsPrint(
                        take.act(
                            new RqWithHeader(new RqFake(), "Accept", "text/xml")
                        )
                    ).body()
                ).asString()
            ),
            XhtmlMatchers.hasXPaths(
                "/page/millis",
                "/page/recent[count(talk)=2]",
                "/page/links/link[@rel='ticks']",
                "/page/links/link[@rel='takes:github']",
                "/page/toggles/read-only"
            )
        );
    }

}
