/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;

/**
 * An agent that rethrows an error with information about the talk.
 *
 * @since 1.59
 */
public final class VerboseAgent implements Agent {

    /**
     * Agent.
     */
    private final transient Agent origin;

    /**
     * Ctor.
     * @param agent Original agent
     */
    public VerboseAgent(final Agent agent) {
        this.origin = agent;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void execute(final Talk talk) throws IOException {
        try {
            this.origin.execute(talk);
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            throw new IllegalArgumentException(
                String.format(
                    "In the talk at https://www.rultor.com/t/%d",
                    talk.number()
                ),
                ex
            );
        }
    }
}
