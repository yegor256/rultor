/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.spi;

import com.jcabi.aspects.Immutable;

/**
 * Tick.
 *
 * @since 1.52
 */
@Immutable
public final class Tick {

    /**
     * When was it started.
     */
    private final transient long when;

    /**
     * Duration.
     */
    private final transient long msec;

    /**
     * Talks processed or -1.
     */
    private final transient int talks;

    /**
     * Ctor.
     * @param date When
     * @param duration Duration in msec
     * @param total Total processed or negative if failed
     */
    public Tick(final long date, final long duration,
        final int total) {
        this.when = date;
        this.msec = duration;
        this.talks = total;
    }

    /**
     * Time of start.
     * @return Time of start
     */
    public long start() {
        return this.when;
    }

    /**
     * Duration in msec.
     * @return Duration
     */
    public long duration() {
        return this.msec;
    }

    /**
     * Total processed or negative.
     * @return Number of talks
     */
    public int total() {
        return this.talks;
    }

}
