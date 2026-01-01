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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for ${@link YamlXML}.
 *
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
            new YamlXML("a: test\nb: 'hello'\nc:\n  - one\nd:\n  f: e").get(),
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
            new YamlXML("a: alpha\nb:\nc:\n  - beta").get(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='a' and .='alpha']",
                "/p/entry[@key='b' and .='']",
                "/p/entry[@key='c']/item[.='beta']"
            )
        );
    }

    /**
     * YamlXML can parse a broken text and throw.
     * @param yaml Test yaml string
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "there\n\t\\/\u0000",
        "first: \"привет \\/\t\r\""
    })
    void parsesBrokenConfigsAndThrows(final String yaml) {
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new YamlXML(yaml).get()
        );
    }

}
