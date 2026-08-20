/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.rultor.Env;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.io.InputStreamOf;
import org.cactoos.text.Joined;

/**
 * Tail daemon output.
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "xml", "hash" })
public final class Tail {

    /**
     * Talk.
     */
    private final transient XML xml;

    /**
     * Hash.
     */
    private final transient String hash;

    /**
     * Ctor.
     * @param talk Talk
     * @param hsh Hash
     */
    public Tail(final XML talk, final String hsh) {
        this.xml = talk;
        this.hash = hsh;
    }

    /**
     * Read it.
     * @return Stream with log
     * @throws IOException If fails
     */
    @SuppressWarnings("unchecked")
    public InputStream read() throws IOException {
        final Collection<Map.Entry<String, Connect>> connects =
            Arrays.asList(
                new AbstractMap.SimpleEntry<>(
                    String.format(
                        "/talk/archive/log[@id='%s' and starts-with(.,'s3:')]",
                        this.hash
                    ),
                    new S3Connect(this.xml, this.hash)
                ),
                new AbstractMap.SimpleEntry<>(
                    String.format(
                        "/talk[shell and daemon[@id='%s'] and daemon/dir]",
                        this.hash
                    ),
                    new SshConnect(this.xml)
                ),
                new AbstractMap.SimpleEntry<>(
                    "/talk[daemon[@id='00000000'] and daemon/dir]",
                    new FakeConnect(this.xml)
                ),
                new AbstractMap.SimpleEntry<>(
                    "/talk",
                    () -> new InputStreamOf(
                        new Joined(
                            "",
                            "rultor.com ",
                            Env.read("Rultor-Version"),
                            "/",
                            Env.read("Rultor-Version"),
                            System.lineSeparator(),
                            "nothing yet, try again in 15 seconds"
                        ),
                        StandardCharsets.UTF_8
                    )
                )
            );
        InputStream stream = null;
        for (final Map.Entry<String, Connect> ent : connects) {
            if (!this.xml.nodes(ent.getKey()).isEmpty()) {
                stream = ent.getValue().read();
                break;
            }
        }
        if (stream == null) {
            throw new IllegalArgumentException("internal error");
        }
        return stream;
    }
}
