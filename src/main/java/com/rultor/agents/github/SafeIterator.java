/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
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
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public boolean hasNext() {
        boolean has;
        try {
            has = this.origin.hasNext();
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            has = false;
            Logger.error(this, "hasNext(): %[exception]s", ex);
        }
        return has;
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
