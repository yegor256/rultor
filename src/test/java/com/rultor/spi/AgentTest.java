/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link Agent}.
 *
 * @since 1.50.0
 */
final class AgentTest {

    @Test
    void skipsIfMatches() throws Exception {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives()
                .xpath("/talk")
                .attr("name", "yegor256/rultor")
        );
        new Agent.SkipIfName(null, "^(zerocracy|yegor256)/.*$").execute(talk);
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new Agent.SkipIfName(null, "^abc/.*$").execute(talk)
        );
    }

}
