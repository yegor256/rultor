/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import co.stateful.RtSttc;
import com.jcabi.github.RtGitHub;
import com.jcabi.urn.URN;
import com.yegor256.WeAreOnline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link Entry}.
 * @since 1.58
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(WeAreOnline.class)
final class EntryTest {

    /**
     * RtSttc can work in production mode.
     *
     * <p>This test is actually checking not how RtSttc works, but
     * whether it can work in current environment, with full list
     * of project dependencies. If there will be any dependency issue,
     * this test will crash with a different exception, not AssertionError.
     */
    @Test
    void sttcConnects() {
        Assertions.assertThrows(
            AssertionError.class,
            () -> new RtSttc(
                URN.create("urn:test:1"),
                "invalid-token"
            ).counters().names()
        );
    }

    /**
     * RtGitHub can work in production mode.
     *
     * <p>This test is actually checking not how RtGithug works, but
     * whether it can work in current environment, with full list
     * of project dependencies. If there will be any dependency issue,
     * this test will crash with a different exception, not AssertionError.
     */
    @Test
    void githubConnects() {
        Assertions.assertThrows(
            AssertionError.class,
            () -> new RtGitHub("intentionally-invalid-token")
                .users().self().login()
        );
    }
}
