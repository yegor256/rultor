/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.log.Logger;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import io.sentry.Sentry;

/**
 * Safe agent.
 * @since 1.59
 */
public final class SafeAgent implements Agent {

    /**
     * Agent.
     */
    private final transient Agent origin;

    /**
     * Ctor.
     * @param agent Original agent
     */
    public SafeAgent(final Agent agent) {
        this.origin = agent;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void execute(final Talk talk) {
        try {
            this.origin.execute(talk);
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            Logger.error(
                this, "execute(): %s throws %[exception]s",
                this.origin.getClass().getCanonicalName(), ex
            );
            Sentry.captureException(ex);
        }
    }
}
