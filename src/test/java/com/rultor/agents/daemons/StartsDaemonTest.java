/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StartsDaemon}.
 *
 * @since 2.0
 */
final class StartsDaemonTest {

    /**
     * StartsDaemon can reject missing GPG secring.
     */
    @Test
    void rejectsMissingGpgSecring() {
        MatcherAssert.assertThat(
            "Missing GPG_SECRING should be reported without NPE",
            Assertions.assertThrows(
                IOException.class,
                () -> StartsDaemon.checkedSecring(null)
            ).getMessage(),
            Matchers.allOf(
                Matchers.containsString("missing"),
                Matchers.containsString("GPG_SECRING")
            )
        );
    }
}
