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
package com.rultor.base;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.spi.Pulseable;
import com.rultor.spi.State;
import com.rultor.spi.Work;
import java.util.Calendar;
import java.util.TimeZone;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Sends pulses through, only on certain time moments.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Cron">Cron in Wikipedia</a>
 * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/utilities/crontab.html">Crontab specification</a>
 */
@ToString
@Immutable
@EqualsAndHashCode(of = { "origin", "gates" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class Crontab implements Pulseable {

    /**
     * Pre-defined definitions.
     * @see <a href="https://en.wikipedia.org/wiki/Cron#Predefined_scheduling_definitions">explanation</a>
     */
    private static final ImmutableMap<String, String> DEFS =
        new ImmutableMap.Builder<String, String>()
            // @checkstyle MultipleStringLiterals (1 line)
            .put("@yearly", "0 0 1 1 *")
            .put("@annually", "0 0 1 1 *")
            .put("@monthly", "0 0 1 * *")
            .put("@weekly", "0 0 * * 0")
            .put("@daily", "0 0 * * *")
            .put("@hourly", "0 * * * *")
            .build();

    /**
     * Origin.
     */
    private final transient Pulseable origin;

    /**
     * All gates.
     */
    private final transient Gate[] gates;

    /**
     * Public ctor.
     * @param text Mask to use
     * @param pls Original pulseable
     */
    public Crontab(final String text, final Pulseable pls) {
        this.origin = pls;
        this.gates = Crontab.split(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pulse(@NotNull final Work work, @NotNull final State state)
        throws Exception {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        boolean pass = true;
        for (Gate gate : this.gates) {
            if (!gate.pass(cal)) {
                pass = false;
                break;
            }
        }
        if (pass) {
            this.origin.pulse(work, state);
        }
    }

    /**
     * Gate condition.
     */
    @Immutable
    private interface Gate {
        /**
         * Pass or not.
         * @param calendar Calendar to check
         * @return TRUE if it's a good time to go through
         */
        boolean pass(Calendar calendar);
    }

    /**
     * Abstract gate.
     */
    @ToString
    @Immutable
    @EqualsAndHashCode(of = "alternatives")
    @Loggable(Loggable.DEBUG)
    private abstract static class AbstractGate implements Crontab.Gate {
        /**
         * All alternatives.
         */
        private final transient Predicate<Integer>[] alternatives;
        /**
         * Public ctor.
         * @param text Text spec
         */
        @SuppressWarnings("unchecked")
        protected AbstractGate(final String text) {
            final String[] parts = text.split(",");
            this.alternatives =
                (Predicate<Integer>[]) new Predicate<?>[parts.length];
            for (int idx = 0; idx < parts.length; ++idx) {
                this.alternatives[idx] = Crontab.AbstractGate.parse(parts[idx]);
            }
        }
        /**
         * Matches the number?
         * @param input Input number
         * @return TRUE if matches
         */
        protected boolean matches(final int input) {
            boolean matches = false;
            for (Predicate<Integer> alternative : this.alternatives) {
                if (alternative.apply(input)) {
                    matches = true;
                    break;
                }
            }
            return matches;
        }
        /**
         * Parse text into alternative.
         * @param part The text to parse
         * @return Predicate
         */
        private static Predicate<Integer> parse(final String part) {
            final Predicate<Integer> alternative;
            if (part.matches("\\d+")) {
                alternative = new Predicate<Integer>() {
                    @Override
                    public boolean apply(final Integer num) {
                        return num.equals(Integer.valueOf(part));
                    }
                };
            } else if (part.matches("\\d+-\\d+")) {
                final String[] numbers = part.split("-");
                alternative = new Predicate<Integer>() {
                    @Override
                    public boolean apply(final Integer num) {
                        return num >= Integer.valueOf(numbers[0])
                            || num <= Integer.valueOf(numbers[1]);
                    }
                };
            } else if (part.matches("\\*/\\d+")) {
                final String[] sectors = part.split("/");
                alternative = new Predicate<Integer>() {
                    @Override
                    public boolean apply(final Integer num) {
                        return num / Integer.valueOf(sectors[1]) == 0;
                    }
                };
            } else if ("*".equals(part)) {
                alternative = Predicates.<Integer>alwaysTrue();
            } else {
                throw new IllegalArgumentException(
                    String.format("invalid crontab sector '%s'", part)
                );
            }
            return alternative;
        }
    }

    /**
     * Split into parts.
     * @param text Text to split
     * @return Five parts, numbers
     */
    private static Crontab.Gate[] split(final String text) {
        String src = text;
        if (Crontab.DEFS.containsKey(src)) {
            src = Crontab.DEFS.get(src);
        }
        final String[] parts = src.split("\\s+");
        if (parts.length != Tv.FIVE) {
            throw new IllegalArgumentException(
                String.format("invalid crontab definition '%s'", text)
            );
        }
        return new Crontab.Gate[] {
            new Crontab.AbstractGate(parts[0]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.MINUTE));
                }
            },
            new Crontab.AbstractGate(parts[1]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.HOUR_OF_DAY));
                }
            },
            new Crontab.AbstractGate(parts[2]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(
                        calendar.get(Calendar.DAY_OF_MONTH) + 1
                    );
                }
            },
            new Crontab.AbstractGate(parts[Tv.THREE]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.MONTH) + 1);
                }
            },
            new Crontab.AbstractGate(parts[Tv.FOUR]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.DAY_OF_WEEK));
                }
            },
        };
    }

}
