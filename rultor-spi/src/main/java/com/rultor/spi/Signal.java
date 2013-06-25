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
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Signal in the pulse.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "mnemo", "right" })
@Loggable(Loggable.DEBUG)
public final class Signal {

    /**
     * Mnemo.
     */
    public enum Mnemo {
        /**
         * Spec declared.
         */
        SPEC,
        /**
         * Stage started.
         */
        START,
        /**
         * Stage finished successfully.
         */
        SUCCESS,
        /**
         * Stage failed.
         */
        FAILURE;
    };

    /**
     * Pattern to match.
     */
    private static final Pattern PATTERN = Pattern.compile(
        // @checkstyle LineLength (1 line)
        ".*RULTOR:(\\d+):(SPEC|START|SUCCESS|FAILURE):([\\p{ASCII}&&[^\\p{Cntrl}]]*)"
    );

    /**
     * Mnemo.
     */
    private final transient Signal.Mnemo mnemo;

    /**
     * Value.
     */
    private final transient String right;

    /**
     * Public ctor.
     * @param name Key/mnemo
     * @param val Value
     */
    public Signal(@NotNull final Signal.Mnemo name, @NotNull final String val) {
        this.mnemo = name;
        this.right = val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String escaped = StringEscapeUtils.escapeJava(this.right);
        return new StringBuilder()
            .append("RULTOR")
            .append(':')
            .append(escaped.length())
            .append(':')
            .append(this.mnemo)
            .append(':')
            .append(escaped)
            .toString();
    }

    /**
     * Key.
     * @return The key
     */
    public Signal.Mnemo key() {
        return this.mnemo;
    }

    /**
     * Value.
     * @return The value
     */
    public String value() {
        return this.right;
    }

    /**
     * Parse it from the string.
     * @param text Text to parse
     * @return Signal, if parsed
     */
    public static Signal valueOf(final String text) {
        final Matcher mtr = Signal.PATTERN.matcher(text);
        if (!mtr.matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "signal not found in '%s', use exists() first",
                    text
                )
            );
        }
        if (Integer.parseInt(mtr.group(1))
            != mtr.group(Tv.THREE).length()) {
            throw new IllegalArgumentException(
                String.format(
                    "matcher counter mismatch in '%s', use exists() first",
                    text
                )
            );
        }
        return new Signal(
            Signal.Mnemo.valueOf(mtr.group(2)),
            StringEscapeUtils.unescapeJava(mtr.group(Tv.THREE).trim())
        );
    }

    /**
     * Whether the text contains a signal.
     * @param text Text to parse
     * @return TRUE if signal is present
     */
    public static boolean exists(final String text) {
        final Matcher mtr = Signal.PATTERN.matcher(text);
        return mtr.matches() && Integer.parseInt(
            mtr.group(1)
        ) == mtr.group(Tv.THREE).length();
    }

    /**
     * Convenient utility method to log.
     * @param mnemo Mnemo to log
     * @param fmt Format
     * @param args Optional arguments
     */
    public static void log(final Signal.Mnemo mnemo,
        final String fmt, final Object... args) {
        Logger.info(
            Signal.class,
            new Signal(mnemo, Logger.format(fmt, args)).toString()
        );
    }

}
