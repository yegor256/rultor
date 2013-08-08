/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.spi;

import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArraySortedSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Vector of pulses.
 *
 * @param <T> Type of elements to page
 * @param <K> Type of positioning element
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Pageable<T, K> extends Iterable<T> {

    /**
     * Get a subset of this vector.
     * @param head Maximum time that is allowed in the result vector
     * @return Similar vector, that contains only pulses that are older than
     *  or equal to the provided date
     * @throws IOException If fails with some IO problem
     */
    @NotNull(message = "pulses are never NULL")
    Pageable<T, K> tail(@NotNull(message = "head can't be NULL") K head)
        throws IOException;

    /**
     * Immutable collection, based on array.
     * @param <T> Type of elements
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Array<T> implements Pageable<T, T> {
        /**
         * Encapsulated array.
         */
        private final transient ArraySortedSet<T> times;
        /**
         * Public ctor.
         */
        public Array() {
            this(new ArrayList<T>(0));
        }
        /**
         * Public ctor.
         * @param array Array of data
         */
        public Array(@NotNull(message = "array can't be NULL")
            final Collection<T> array) {
            this.times = new ArraySortedSet<T>(
                array, new ArraySortedSet.Comparator.Reverse<T>()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public Pageable<T, T> tail(
            @NotNull(message = "head is NULL") final T head) {
            return new Pageable.Array<T>(this.times.tailSet(head));
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<T> iterator() {
            return this.times.iterator();
        }
    }

    /**
     * Sequence of pulses.
     * @param <T> Type of elements
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Sequence<T> implements Pageable<T, T> {
        /**
         * First.
         */
        private final transient Pageable<T, T> first;
        /**
         * Second.
         */
        private final transient Pageable<T, T> second;
        /**
         * Public ctor.
         * @param frst First
         * @param scnd Second
         */
        public Sequence(
            @NotNull(message = "first can't be NULL") final Pageable<T, T> frst,
            @NotNull(message = "second can't be NULL")
            final Pageable<T, T> scnd) {
            this.first = frst;
            this.second = scnd;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public Pageable<T, T> tail(
            @NotNull(message = "head can't be NULL") final T head)
            throws IOException {
            return new Pageable.Sequence<T>(
                this.first.tail(head), this.second.tail(head)
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public Iterator<T> iterator() {
            return Iterators.mergeSorted(
                Arrays.asList(this.first.iterator(), this.second.iterator()),
                new Comparator<T>() {
                    @Override
                    public int compare(final T left, final T right) {
                        return Comparable.class.cast(right).compareTo(left);
                    }
                }
            );
        }
    }

}
