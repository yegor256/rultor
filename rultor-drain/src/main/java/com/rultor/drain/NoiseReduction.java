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
package com.rultor.drain;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;

/**
 * Noise reduction.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "work", "pattern", "visible", "dirty", "clean" })
@Loggable(Loggable.DEBUG)
public final class NoiseReduction implements Drain {

    /**
     * Static set of clean drains.
     */
    private static final Set<Drain> GOOD = new CopyOnWriteArraySet<Drain>();

    /**
     * Static set of dirty drains.
     */
    private static final Set<Drain> BAD = new CopyOnWriteArraySet<Drain>();

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Regular expression pattern to match.
     */
    private final transient String pattern;

    /**
     * How many dirty pulses should be visible.
     */
    private final transient int visible;

    /**
     * Dirty pulses.
     */
    private final transient Drain dirty;

    /**
     * Clean pulses.
     */
    private final transient Drain clean;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param ptn Regular
     * @param vsbl How many items should be visible from dirty drain
     * @param drt Dirty drain
     * @param cln Clean drain
     * @checkstyle ParameterNumber (10 lines)
     */
    public NoiseReduction(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "pattern can't be NULL") final String ptn,
        final int vsbl,
        @NotNull(message = "dirty drain can't be NULL") final Drain drt,
        @NotNull(message = "clean drain can't be NULL") final Drain cln) {
        this.work = wrk;
        this.pattern = ptn;
        this.visible = vsbl;
        this.dirty = drt;
        this.clean = cln;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s with noise reduction by `%s` (%d visible) buffering in %s",
            this.clean,
            this.pattern,
            this.visible,
            this.dirty
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        // @checkstyle AnonInnerLength (50 lines)
        return new Pageable<Time, Time>() {
            @Override
            public Pageable<Time, Time> tail(final Time head)
                throws IOException {
                return NoiseReduction.this.clean.pulses().tail(head);
            }
            @Override
            public Iterator<Time> iterator() {
                try {
                    return new NoiseReduction.Distinct<Time>(
                        Iterables.concat(
                            Iterables.limit(
                                NoiseReduction.this.dirty.pulses(),
                                NoiseReduction.this.visible
                            ),
                            NoiseReduction.this.clean.pulses()
                        ).iterator()
                    );
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines)
        throws IOException {
        final boolean matched;
        synchronized (NoiseReduction.GOOD) {
            matched = !NoiseReduction.GOOD.contains(this.dirty)
                && Iterables.any(
                    lines,
                    new Predicate<String>() {
                        @Override
                        public boolean apply(final String input) {
                            return input.matches(NoiseReduction.this.pattern);
                        }
                    }
                );
            if (matched) {
                NoiseReduction.GOOD.add(this.dirty);
            }
        }
        if (matched && NoiseReduction.BAD.contains(this.dirty)) {
            this.clean.append(
                ImmutableList.copyOf(
                    IOUtils.lineIterator(this.dirty.read(), CharEncoding.UTF_8)
                )
            );
        }
        if (NoiseReduction.GOOD.contains(this.dirty)) {
            this.clean.append(lines);
        } else {
            this.dirty.append(lines);
            NoiseReduction.BAD.add(this.dirty);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        final boolean exists = Iterables.contains(
            Iterables.limit(this.dirty.pulses(), this.visible),
            this.work.scheduled()
        );
        InputStream stream;
        if (exists) {
            stream = this.dirty.read();
        } else {
            stream = this.clean.read();
        }
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "NoiseReduction: exists=%B, pattern='%s', visible=%d, dirty='%s', clean='%s'\n",
                    exists,
                    this.pattern,
                    this.visible,
                    this.dirty,
                    this.clean
                )
            ),
            stream
        );
    }

    /**
     * Distinct iterator, not thread-safe.
     * @see https://code.google.com/p/guava-libraries/issues/detail?id=1464
     */
    private static final class Distinct<T> implements Iterator<T> {
        /**
         * Original iterator.
         */
        private final transient Iterator<T> origin;
        /**
         * Set of elements already seen.
         */
        private final transient Set<T> seen = new LinkedHashSet<T>(0);
        /**
         * Recent element.
         */
        private final transient AtomicReference<T> recent =
            new AtomicReference<T>();
        /**
         * Ctor.
         * @param iterator Original iterator
         */
        protected Distinct(final Iterator<T> iterator) {
            this.origin = iterator;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            while (this.recent.get() == null && this.origin.hasNext()) {
                final T next = this.origin.next();
                if (!this.seen.contains(next)) {
                    this.seen.add(next);
                    this.recent.set(next);
                }
            }
            return this.recent.get() != null;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public T next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            return this.recent.getAndSet(null);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
