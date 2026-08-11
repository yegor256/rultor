/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import io.sentry.Sentry;
import java.io.IOException;

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
    public void execute(final Talk talk) {
        new VerboseRunnable(
            () -> {
                try {
                    this.origin.execute(talk);
                } catch (final IOException ex) {
                    Logger.error(
                        this, "execute(): %s throws %[exception]s",
                        this.origin.getClass().getCanonicalName(), ex
                    );
                    Sentry.captureException(ex);
                }
            },
            true
        ).run();
    }
}
