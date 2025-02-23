/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.Array;
import com.jcabi.xml.XML;
import com.rultor.spi.Talk;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The agent is required for this talk?
 *
 * @since 1.74
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "xpaths")
public final class Required {

    /**
     * Encapsulated XPaths.
     */
    private final transient Array<String> xpaths;

    /**
     * Ctor.
     * @param args XPath expressions
     */
    public Required(final Array<String> args) {
        this.xpaths = args;
    }

    /**
     * This talk is required for this agent?
     * @param talk The talk
     * @return TRUE if this talk is required for this agent
     * @throws IOException If fails
     */
    public boolean isIt(final Talk talk) throws IOException {
        final XML xml = talk.read();
        boolean good = true;
        for (final String xpath : this.xpaths) {
            if (xml.nodes(xpath).isEmpty()) {
                good = false;
                break;
            }
        }
        return good;
    }

}
