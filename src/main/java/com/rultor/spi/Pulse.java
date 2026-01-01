/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.spi;

import java.util.Collections;

/**
 * Pulse.
 *
 * @since 1.20
 */
public interface Pulse {

    /**
     * Empty.
     * @checkstyle AnonInnerLengthCheck (24 lines)
     */
    Pulse EMPTY = new Pulse() {
        @Override
        public void add(final Tick tick) {
            throw new UnsupportedOperationException("#add()");
        }

        @Override
        public Iterable<Tick> ticks() {
            return Collections.emptyList();
        }

        @Override
        public Iterable<Throwable> error() {
            return Collections.emptyList();
        }

        @Override
        public void error(final Iterable<Throwable> errors) {
            throw new UnsupportedOperationException("#error()");
        }
    };

    /**
     * Add new tick.
     * @param tick The tick
     */
    void add(Tick tick);

    /**
     * Get ticks.
     * @return Ticks
     */
    Iterable<Tick> ticks();

    /**
     * Most recent exception (or empty).
     * @return Problems
     */
    Iterable<Throwable> error();

    /**
     * Set recent exception (or empty).
     * @param errors Errors or empty if none
     */
    void error(Iterable<Throwable> errors);

}
