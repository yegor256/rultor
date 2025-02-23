/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Brackets}.
 *
 * @since 1.2
 */
final class BracketsTest {
    /**
     * Brackets can fetch environment vars.
     * @throws Exception In case of error.
     */
    @Test
    void escapesInput() {
        final Brackets brackets = new Brackets(
            new ListOf<>(
                "Elegant",
                "Objects"
            )
        );
        MatcherAssert.assertThat(
            "Each element should be wrapped in ''",
            brackets.toString(),
            Matchers.equalTo("( 'Elegant' 'Objects' )")
        );
    }
}
