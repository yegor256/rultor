/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Profile;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link YamlXML}.
 * @since 1.0
 * @checkstyle AbbreviationAsWordInNameCheck (5 lines)
 */
final class YamlXMLTest {

    /**
     * YamlXML can parse.
     */
    @Test
    void parsesYamlConfig() {
        MatcherAssert.assertThat(
            "yml should be parsed to xml",
            new YamlXML(
                String.format(
                    "a: test%nb: 'hello'%nc:%n  - one%nd:%n  f: e"
                )
            ).get(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='a' and .='test']",
                "/p/entry[@key='b' and .='hello']",
                "/p/entry[@key='c']/item[.='one']",
                "/p/entry[@key='d']/entry[@key='f' and .='e']"
            )
        );
    }

    /**
     * YamlXML can parse a broken text.
     */
    @Test
    void parsesYamlConfigWhenBroken() {
        MatcherAssert.assertThat(
            "empty values should be kept",
            new YamlXML(
                String.format(
                    "a: alpha%nb:%nc:%n  - beta"
                )
            ).get(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='a' and .='alpha']",
                "/p/entry[@key='b' and .='']",
                "/p/entry[@key='c']/item[.='beta']"
            )
        );
    }

    /**
     * YamlXML can parse a broken text with a stray control character and throw.
     */
    @Test
    void parsesBrokenConfigWithControlCharAndThrows() {
        final String yaml = String.format("there%n\t\\/\0");
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new YamlXML(yaml).get()
        );
    }

    /**
     * YamlXML can parse a broken text with a stray carriage return and throw.
     */
    @Test
    void parsesBrokenConfigWithCarriageReturnAndThrows() {
        final String yaml = "first: \"привет \\/\t\015\"";
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new YamlXML(yaml).get()
        );
    }
}
