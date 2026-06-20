/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.rultor.Time;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.net.UnknownHostException;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link EndsDaemon}.
 *
 * @since 1.2
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 */
final class EndsDaemonTest {

    /**
     * EndsDaemon should fail if host is not found.
     * @throws IOException In case of error.
     */
    @Test
    void failsIfHostNotFound() throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon")
                .attr("id", "abcd")
                .add("title").set("merge").up()
                .add("script").set("ls").up()
                .add("started").set(new Time().iso()).up()
                .add("dir").set("/tmp").up()
                .up()
                .add("shell").attr("id", "a1b2c3e3")
                .add("host").set("bad-host-name").up()
                .add("port").set("2222").up()
                .add("login").set("test").up()
                .add("key").set("test")
        );
        final Agent agent = new EndsDaemon();
        Assertions.assertThrows(
            UnknownHostException.class,
            () -> agent.execute(talk)
        );
    }

    /**
     * EndsDaemon limits tail by its final length.
     */
    @Test
    void limitsTailByLength() {
        final String end = "tail-end";
        final String tail = EndsDaemon.tail(
            new ListOf<>(
                new TextOf(String.format("tail-start-%s", "x".repeat(12_000))),
                new TextOf(end)
            )
        );
        Assertions.assertEquals(
            10_000,
            tail.length(),
            "Tail should be capped by length"
        );
        Assertions.assertTrue(
            tail.endsWith(end),
            "Tail should preserve the end of stdout"
        );
        Assertions.assertFalse(
            tail.contains("tail-start"),
            "Tail should not keep the beginning when stdout is too long"
        );
    }

}
