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

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulses;
import com.rultor.spi.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Iterator;
import java.util.Scanner;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
public final class NoiseReduction implements Drain {

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
     * @param ptn Regular
     * @param vsbl How many items should be visible from dirty drain
     * @param drt Dirty drain
     * @param cln Clean drain
     * @checkstyle ParameterNumber (4 lines)
     */
    public NoiseReduction(@NotNull final String ptn, final int vsbl,
        @NotNull final Drain drt, @NotNull final Drain cln) {
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
    public Pulses pulses() throws IOException {
        final Pulses drt = this.dirty.pulses();
        final Pulses cln = this.clean.pulses();
        return new Pulses() {
            @Override
            public Pulses tail(final Time head) {
                return cln.tail(head);
            }
            @Override
            public Iterator<Time> iterator() {
                return Iterables.concat(
                    Iterables.limit(drt, NoiseReduction.this.visible), cln
                ).iterator();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines)
        throws IOException {
        this.dirty.append(lines);
        final Scanner scanner = new Scanner(this.dirty.read());
        if (scanner.findWithinHorizon(this.pattern, 0) != null) {
            this.clean.append(lines);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        InputStream stream = this.clean.read();
        if (stream.available() == 0) {
            stream = this.dirty.read();
        }
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "NoiseReduction: pattern='%s', visible=%d, dirty='%s', clean='%s'\n",
                    this.pattern,
                    this.visible,
                    this.dirty,
                    this.clean
                )
            ),
            stream
        );
    }

}
