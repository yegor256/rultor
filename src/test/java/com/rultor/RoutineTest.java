/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import co.stateful.mock.MkSttc;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.spi.Pulse;
import com.rultor.spi.Talks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Routine}.
 *
 * @since 2.0
 */
final class RoutineTest {

    @Test
    void survivesInterruptionOfItsThread() throws Exception {
        try (
            Routine routine = new Routine(
                new Talks.InDir(), Pulse.EMPTY, new MkGitHub(), new MkSttc()
            )
        ) {
            Thread.currentThread().interrupt();
            Assertions.assertDoesNotThrow(
                routine::run,
                "Routine must not let an interruption kill its schedule"
            );
        }
    }
}
