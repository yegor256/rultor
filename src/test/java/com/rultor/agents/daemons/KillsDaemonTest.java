/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.Time;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link KillsDaemon}.
 *
 * @since 1.0
 */
final class KillsDaemonTest {

    /**
     * KillsDaemon can ignore a daemon.
     * @throws Exception In case of error.
     */
    @Test
    void ignoresFreshDaemon() throws Exception {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("daemon")
                .attr("id", "abcd")
                .add("script").set("empty").up()
                .add("title").set("something new").up()
                .add("started")
                .set(new Time().iso())
        );
        final Agent agent = new KillsDaemon();
        agent.execute(talk);
        MatcherAssert.assertThat(
            "KillsDaemon stops daemon older then 1h by default",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk/daemon")
        );
    }

}
