/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.spi;

import com.jcabi.matchers.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link Talk}.
 *
 * @since 1.41.3
 */
final class TalkTest {

    /**
     * Talk can accept correct XML.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsValidXml() throws Exception {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives()
                .xpath("/talk").add("wire")
                .add("github-repo").set("test/test").up()
                .add("github-issue").set("123").up()
                .add("href").set("#").up().up()
                .add("daemon").attr("id", "abc")
                .add("script").set("hello!").up()
                .add("title").set("some title here \u20ac").up()
                .add("dir").set("C:\\Windows32\\Temp_One").up()
        );
        MatcherAssert.assertThat(
            "All info should be in the talk",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk/wire")
        );
    }

}
