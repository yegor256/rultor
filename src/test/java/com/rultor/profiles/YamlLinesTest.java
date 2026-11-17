/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import java.util.Collections;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link YamlLines}.
 *
 * @since 2.1
 */
final class YamlLinesTest {

    /**
     * YamlLines can find lines of all entries.
     *
     * @throws Exception In case of error.
     */
    @Test
    void findsLinesOfEntries() throws Exception {
        MatcherAssert.assertThat(
            "Lines must match positions of entries in the document",
            new YamlLines(
                new Joined(
                    System.lineSeparator(),
                    "merge:",
                    "  script:",
                    "    - pwd",
                    "    - ls",
                    "docker:",
                    "  as_root: true"
                ).asString()
            ).lines(),
            Matchers.allOf(
                Matchers.hasEntry("", 1),
                Matchers.hasEntry("/merge", 1),
                Matchers.hasEntry("/merge/script", 2),
                Matchers.hasEntry("/merge/script/0", 3),
                Matchers.hasEntry("/merge/script/1", 4),
                Matchers.hasEntry("/docker", 5),
                Matchers.hasEntry("/docker/as_root", 6)
            )
        );
    }

    /**
     * YamlLines can survive an empty document.
     */
    @Test
    void survivesEmptyDocument() {
        MatcherAssert.assertThat(
            "Empty document must have no lines",
            new YamlLines("").lines(),
            Matchers.equalTo(Collections.emptyMap())
        );
    }
}
