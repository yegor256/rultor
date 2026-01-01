/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link DropsTalk}.
 *
 * @since 1.3
 */
final class DropsTalkTest {

    /**
     * Removes 'later' attr.
     * @throws Exception In case of error.
     */
    @Test
    void dropsLostTalk() throws Exception {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .attr("later", "true")
        );
        final Agent agent = new DropsTalk();
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Talk should be not with later after DropsTalk",
            talk.read(),
            XhtmlMatchers.hasXPaths("/talk[@later='false']")
        );
    }

}
