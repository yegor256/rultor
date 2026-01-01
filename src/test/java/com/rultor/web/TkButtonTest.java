/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.matchers.XhtmlMatchers;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkButton}.
 * @since 1.50
 */
final class TkButtonTest {

    /**
     * TkButton can render SVG.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersSvg() throws Exception {
        final TkRegex take = new TkButton();
        MatcherAssert.assertThat(
            "Button in svg format should be generated",
            XhtmlMatchers.xhtml(
                new TextOf(
                    new RsPrint(
                        take.act(new RqRegex.Fake("(.*)", "hey"))
                    ).body()
                ).asString()
            ),
            XhtmlMatchers.hasXPaths(
                "/svg:svg",
                "//svg:svg[count(svg:rect) >= 2]"
            )
        );
    }

}
