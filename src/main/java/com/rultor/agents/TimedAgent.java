/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.log.Logger;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Aggent that tracks time and complains if too slow.
 * @since 1.59
 */
public final class TimedAgent implements Agent {

    /**
     * Limit in seconds.
     */
    private static final long LIMIT = TimeUnit.SECONDS.toMillis(30L);

    /**
     * Agent.
     */
    private final transient Agent origin;

    /**
     * Ctor.
     * @param agent Original agent
     */
    public TimedAgent(final Agent agent) {
        this.origin = agent;
    }

    @Override
    public void execute(final Talk talk) throws IOException {
        final long start = System.currentTimeMillis();
        this.origin.execute(talk);
        final long msec = System.currentTimeMillis() - start;
        if (msec > TimedAgent.LIMIT) {
            Logger.error(
                this, "%s#execute() took %[ms]s, it's too long!",
                this.origin.getClass().getCanonicalName(),
                msec
            );
        }
    }
}
