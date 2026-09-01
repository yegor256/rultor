/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Fake file connect.
 * @since 1.1
 */
@Immutable
final class FakeConnect implements Connect {

    /**
     * XML of the talk.
     */
    private final transient XML xml;

    /**
     * Ctor.
     * @param talk Talk
     */
    FakeConnect(final XML talk) {
        this.xml = talk;
    }

    @Override
    public InputStream read() throws IOException {
        return Files.newInputStream(
            Paths.get(this.xml.xpath("/talk/daemon/dir/text() ").get(0))
        );
    }
}
