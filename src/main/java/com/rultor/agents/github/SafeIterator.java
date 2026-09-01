/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.VerboseRunnable;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Safe iterator.
 * @param <T> Class to iterate
 * @since 1.59
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
final class SafeIterator<T> implements Iterator<T> {

    /**
     * Original.
     */
    private final transient Iterator<T> origin;

    /**
     * Ctor.
     * @param itr Original
     */
    SafeIterator(final Iterator<T> itr) {
        this.origin = itr;
    }

    @Override
    public boolean hasNext() {
        final boolean[] has = {false};
        new VerboseRunnable(
            () -> has[0] = this.origin.hasNext(), true
        ).run();
        return has[0];
    }

    @Override
    public T next() {
        return this.origin.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("#remove()");
    }
}
