/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.spi;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Profile}.
 *
 * @since 1.28
 */
final class ProfileTest {

    /**
     * Profile.Fixed can accept correct XML.
     * @throws Exception In case of error.
     * @checkstyle AbbreviationAsWordInNameCheck (5 lines)
     */
    @Test
    void acceptsValidXML() throws Exception {
        MatcherAssert.assertThat(
            "All data should be saved in profile",
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='deploy'>",
                        "<entry key='script'>this is a plain text</entry>",
                        "<entry key='&gt;'><item>hello</item></entry>",
                        "<entry key='abc'><entry key='a'>b</entry></entry>",
                        "</entry></p>"
                    ).asString()
                )
            ).read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='deploy']",
                "/p/entry[@key='deploy']/entry[@key='script']",
                "//entry[@key='script' and .='this is a plain text']"
            )
        );
    }

    /**
     * Profile.Fixed can reject incorrect XML.
     */
    @Test
    void rejectsMixedEntriesAndItems() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='x'><entry key='test'>a</entry>",
                        "<item>b</item></entry></p>"
                    ).asString()
                )
            ).read().inner()
        );
    }

    /**
     * Profile.Fixed can reject incorrect XML.
     */
    @Test
    void rejectsEntryWithoutKey() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Profile.Fixed(
                new XMLDocument("<p><entry>test me</entry></p>")
            ).read().inner()
        );
    }

    /**
     * Profile.Fixed can reject unknown attributes.
     */
    @Test
    void rejectsUnknownAttributes() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new Profile.Fixed(
                new XMLDocument("<p><entry f='x'>x</entry></p>")
            ).read().inner()
        );
    }

}
