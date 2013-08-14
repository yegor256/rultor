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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * ASCII command codes sensitive.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class ASCII implements Drain {

    /**
     * Original drain.
     */
    private final transient Drain origin;

    /**
     * Public ctor.
     * @param drain Main drain
     */
    public ASCII(@NotNull(message = "drain can't be NULL") final Drain drain) {
        this.origin = drain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s sensitive to ASCII command codes",
            this.origin
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return this.origin.pulses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines)
        throws IOException {
        this.origin.append(
            Iterables.transform(
                lines,
                new Function<String, String>() {
                    @Override
                    public String apply(final String input) {
                        return ASCII.decode(input);
                    }
                }
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "ASCII: origin='%s'\n",
                    this.origin
                ),
                CharEncoding.UTF_8
            ),
            this.origin.read()
        );
    }

    /**
     * Decode this line.
     * @param line The line
     * @return Decoded line
     */
    private static String decode(final String line) {
        final StringBuilder output = new StringBuilder();
        for (int idx = 0; idx < line.length(); ++idx) {
            final char chr = line.charAt(idx);
            if (chr == '\015') {
                output.setLength(0);
                continue;
            }
            if (chr == '\010') {
                if (output.length() > 0) {
                    output.setLength(output.length() - 1);
                }
                continue;
            }
            if (chr == '\011') {
                final int lag = Tv.EIGHT - (output.length() % Tv.EIGHT);
                for (int space = 0; space < lag; ++space) {
                    output.append(' ');
                }
                continue;
            }
            output.append(chr);
        }
        return output.toString();
    }

}
