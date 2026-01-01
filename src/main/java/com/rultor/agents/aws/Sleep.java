/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.util.concurrent.TimeUnit;
import lombok.ToString;

/**
 * Sleep.
 *
 * @since 1.77
 */
@Immutable
@ToString
final class Sleep {

    /**
     * Seconds.
     */
    private final transient long seconds;

    /**
     * Ctor.
     * @param scnd Seconds
     */
    Sleep(final long scnd) {
        this.seconds = scnd;
    }

    /**
     * Sleep for a while.
     */
    void now() {
        try {
            Logger.info(this, "Sleeping for %d seconds...", this.seconds);
            Thread.sleep(TimeUnit.SECONDS.toMillis(this.seconds));
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

}
