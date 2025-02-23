/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.Time;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link DismountDaemon}.
 *
 * @since 1.2
 * @checkstyle MultipleStringLiterals (500 lines)
 */
final class DismountDaemonTest {

    /**
     * Sets daemon to ended when host is not responding.
     * @throws IOException In case of error.
     */
    @Test
    void endsIfHostNotFound() throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon")
                .attr("id", "abcd")
                .add("title").set("merge").up()
                .add("script").set("ls").up()
                .add("started").set(new Time(0L).iso()).up()
                .add("dir").set("/tmp").up()
                .up()
                .add("shell").attr("id", "a1b2c3e3")
                .add("host").set("bad-host-name").up()
                .add("port").set("2222").up()
                .add("login").set("test1").up()
                .add("key").set("test1")
        );
        final Agent agent = new DismountDaemon();
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Daemon should be stopped for not found host",
            talk.read(),
            XhtmlMatchers.hasXPaths(
                "/talk[not(daemon)]"
            )
        );
    }

    /**
     * Sets daemon to ended when host is not responding.
     * @throws IOException In case of error.
     */
    @Test
    void ignoresFreshDaemons() throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon")
                .attr("id", "0f4ac")
                .add("title").set("merge").up()
                .add("script").set("ls").up()
                .add("started").set(new Time().iso()).up()
                .add("dir").set("/tmp").up()
                .up()
                .add("shell").attr("id", "a1b2c3e3")
                .add("host").set("bad-host-name").up()
                .add("port").set("2222").up()
                .add("login").set("test-login2").up()
                .add("key").set("test-key2")
        );
        final Agent agent = new DismountDaemon();
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Daemon should not be ended if younger then 10 days",
            talk.read(),
            XhtmlMatchers.hasXPaths("/talk/daemon[not(ended)]")
        );
    }

}
