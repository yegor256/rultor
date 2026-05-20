/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Brackets}.
 * @since 1.2
 */
final class BracketsTest {

    /**
     * Brackets can fetch environment vars.
     */
    @Test
    void escapesInput() {
        MatcherAssert.assertThat(
            "Each element should be wrapped in ''",
            new Brackets(
                new ListOf<>(
                    "Elegant",
                    "Objects"
                )
            ).toString(),
            Matchers.equalTo("( 'Elegant' 'Objects' )")
        );
    }
}
