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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Drain of a rule.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Drain {

    /**
     * Iterate all available pulses, in reverse order (the latest one on top).
     * @return List of them
     * @throws IOException If some IO problem inside
     */
    @NotNull(message = "list of pulses is never NULL")
    Pageable<Time, Time> pulses() throws IOException;

    /**
     * Append a few lines to the drain.
     * @param lines Lines to append
     * @throws IOException If some IO problem inside
     */
    void append(@NotNull(message = "lines can't be NULL")
        Iterable<String> lines) throws IOException;

    /**
     * Read the drain.
     * @return The input stream with data
     * @throws IOException If some IO problem inside
     */
    @NotNull(message = "InputStream is never NULL")
    InputStream read() throws IOException;

    /**
     * Source of the drain.
     */
    @Immutable
    interface Source {
        /**
         * The drain.
         * @return Drain
         */
        @NotNull(message = "drain is never NULL")
        Drain drain();
    }

    /**
     * One line in the drain.
     */
    @Immutable
    interface Line {
        /**
         * Milliseconds from log start.
         * @return Msec
         */
        long msec();
        /**
         * Log level of the line.
         * @return Level
         */
        @NotNull(message = "level is never NULL")
        Level level();
        /**
         * Simple.
         */
        @Immutable
        @EqualsAndHashCode(of = { "when", "lvl", "msg" })
        final class Simple implements Drain.Line {
            /**
             * Pattern to parse it.
             */
            private static final Pattern PTN = Pattern.compile(
                "\\s*(\\d+):(\\d{2})\\s+([A-Z]+) (.*)"
            );
            /**
             * Milliseconds from log start.
             */
            private final transient long when;
            /**
             * Level.
             */
            private final transient String lvl;
            /**
             * Message.
             */
            private final transient String msg;
            /**
             * Public ctor.
             * @param msec When it's happening, msec from start
             * @param level The level
             * @param message The message
             * @checkstyle ParameterNumber (8 lines)
             */
            public Simple(final long msec,
                @NotNull(message = "log level can't be NULL") final Level level,
                @NotNull(message = "message can't be NULL")
                final String message) {
                this.when = msec;
                this.lvl = level.toString();
                this.msg = message;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return String.format(
                    "%3d:%02d %s %s",
                    this.when / DateUtils.MILLIS_PER_MINUTE,
                    (this.when % DateUtils.MILLIS_PER_MINUTE)
                        / DateUtils.MILLIS_PER_SECOND,
                    this.lvl,
                    this.msg
                );
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public long msec() {
                return this.when;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public Level level() {
                return Level.parse(this.lvl);
            }
            /**
             * The text has a line inside.
             * @param text Text to try
             * @return TRUE if it's a line
             */
            public static boolean has(final String text) {
                return Drain.Line.Simple.PTN.matcher(text).matches();
            }
            /**
             * Parse it back to live (or runtime exception if not found).
             * @param text Text to parse
             * @return Line, if possible to get it from there
             */
            public static Drain.Line parse(final String text) {
                final Matcher matcher = Drain.Line.Simple.PTN.matcher(text);
                Validate.isTrue(matcher.matches(), "invalid line '%s'", text);
                return new Drain.Line.Simple(
                    Long.parseLong(
                        matcher.group(1)
                    ) * DateUtils.MILLIS_PER_MINUTE
                    + Long.parseLong(
                        matcher.group(2)
                    ) * DateUtils.MILLIS_PER_SECOND,
                    Level.parse(matcher.group(Tv.THREE)),
                    matcher.group(Tv.FOUR)
                );
            }
        }
    }

}
