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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.rultor.snapshot.Step;
import com.rultor.spi.Instance;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Sends pulses through, only on certain time moments.
 *
 * <p>Use it as an instance wrapper:
 *
 * <pre> com.rultor.base.Crontab(
 *   ${0:?}, "&#65;/5 * * * *",
 *   my-custom-instance
 * )</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @see <a href="https://en.wikipedia.org/wiki/Cron">Cron in Wikipedia</a>
 * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/utilities/crontab.html">Crontab specification</a>
 */
@Immutable
@EqualsAndHashCode(of = { "work", "origin", "gates" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.CyclomaticComplexity" })
public final class Crontab implements Instance {

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
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Origin.
     */
    private final transient Instance origin;

    /**
     * All gates.
     */
    private final transient Array<Crontab.Gate<Calendar>> gates;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param text Mask to use
     * @param instance Original instance
     */
    public Crontab(@NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "crontab text can't be NULL") final String text,
        @NotNull(message = "instance can't be NULL") final Instance instance) {
        this.work = wrk;
        this.origin = instance;
        this.gates = new Array<Crontab.Gate<Calendar>>(Crontab.split(text));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        if (this.allowed()) {
            this.origin.pulse();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format("%s in %[ms]s", this.origin, this.lag(new Time()));
    }

    /**
     * Show all encapsulated rules.
     * @return The text
     */
    public String rules() {
        return StringUtils.join(this.gates, " ");
    }

    /**
     * Lag in milliseconds (how much time left till the next execution).
     *
     * <p>The method is public solely for the sake of unit testing.
     * Otherwise it's almost impossible to test the class.
     *
     * @param date The date to start counting from
     * @return Milliseconds
     */
    public long lag(final Time date) {
        final Calendar today = Crontab.calendar(date);
        long lag = 0;
        for (Crontab.Gate<Calendar> gate : this.gates) {
            lag += gate.lag(today);
        }
        return lag;
    }

    /**
     * Execution allowed?
     * @return TRUE if allowed
     */
    @Step("Crontab `${this.rules()}` #if(!$result)NOT#end allowed execution")
    private boolean allowed() {
        final Calendar today = Crontab.calendar(this.work.scheduled());
        Crontab.Gate<Calendar> denier = null;
        for (Crontab.Gate<Calendar> gate : this.gates) {
            if (!gate.pass(today)) {
                denier = gate;
                break;
            }
        }
        if (denier == null) {
            Logger.info(
                this,
                "Crontab `%s` allows execution at `%s`",
                this.rules(),
                Crontab.moment(this.work.scheduled())
            );
        } else {
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "Not the right moment `%s` for `%s`, see you again in %[ms]s (denied by `%s`)",
                Crontab.moment(this.work.scheduled()),
                this.rules(),
                this.lag(this.work.scheduled()),
                denier
            );
        }
        return denier == null;
    }

    /**
     * Gate condition.
     */
    @Immutable
    private interface Gate<T> {
        /**
         * Pass or not.
         * @param calendar Calendar to check
         * @return TRUE if it's a good time to go through
         */
        boolean pass(T calendar);
        /**
         * How many milliseconds to wait before the next opportunity.
         * @param calendar Calendar to check
         * @return Lag in milliseconds
         */
        long lag(T calendar);
    }

    /**
     * Abstract big gate.
     */
    @Immutable
    @EqualsAndHashCode(of = "alternatives")
    @Loggable(Loggable.DEBUG)
    private abstract static class AbstractBigGate
        implements Crontab.Gate<Calendar> {
        /**
         * All alternatives.
         */
        private final transient Array<Crontab.Gate<Integer>> alternatives;
        /**
         * Public ctor.
         * @param text Text spec
         */
        @SuppressWarnings("unchecked")
        protected AbstractBigGate(final String text) {
            final String[] parts = text.split(",");
            final Collection<Crontab.Gate<Integer>> alts =
                new ArrayList<Crontab.Gate<Integer>>(parts.length);
            for (int idx = 0; idx < parts.length; ++idx) {
                alts.add(Crontab.parse(parts[idx]));
            }
            this.alternatives = new Array<Crontab.Gate<Integer>>(alts);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return StringUtils.join(this.alternatives, "|");
        }
        /**
         * Matches the number?
         * @param input Input number
         * @return TRUE if matches
         */
        protected boolean matches(final int input) {
            boolean matches = false;
            for (Crontab.Gate<Integer> alternative : this.alternatives) {
                if (alternative.pass(input)) {
                    matches = true;
                    break;
                }
            }
            return matches;
        }
        /**
         * Calculate the lag.
         * @param input Input number
         * @return Lag in milliseconds
         */
        protected long lag(final int input) {
            long lag = Long.MAX_VALUE;
            for (Crontab.Gate<Integer> alternative : this.alternatives) {
                lag = Math.min(lag, alternative.lag(input));
            }
            return lag;
        }
    }

    /**
     * Exact gate.
     */
    @Immutable
    @EqualsAndHashCode(of = "exact")
    @Loggable(Loggable.DEBUG)
    private static final class ExactGate implements Crontab.Gate<Integer> {
        /**
         * Exact value to match.
         */
        private final transient int exact;
        /**
         * Protected ctor.
         * @param val The value to encapsulate
         */
        protected ExactGate(final int val) {
            this.exact = val;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return Integer.toString(this.exact);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean pass(final Integer num) {
            return num == this.exact;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long lag(final Integer num) {
            return Math.abs(num - this.exact);
        }
    }

    /**
     * Interval gate.
     */
    @Immutable
    @EqualsAndHashCode(of = { "left", "right" })
    @Loggable(Loggable.DEBUG)
    private static final class IntervalGate implements Crontab.Gate<Integer> {
        /**
         * Left, inclusive.
         */
        private final transient int left;
        /**
         * Right, inclusive.
         */
        private final transient int right;
        /**
         * Protected ctor.
         * @param lft Left, inclusive
         * @param rgt Right, inclusive
         */
        protected IntervalGate(final int lft, final int rgt) {
            Validate.isTrue(
                lft < rgt, "left value %d should be less than right %d",
                lft, rgt
            );
            this.left = lft;
            this.right = rgt;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("%d-%d", this.left, this.right);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean pass(final Integer num) {
            return num >= this.left && num <= this.right;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long lag(final Integer num) {
            long lag = 0;
            if (!this.pass(num)) {
                lag = Math.abs(num - this.left);
            }
            return lag;
        }
    }

    /**
     * Modulo gate.
     */
    @Immutable
    @EqualsAndHashCode(of = "divisor")
    @Loggable(Loggable.DEBUG)
    private static final class ModuloGate implements Crontab.Gate<Integer> {
        /**
         * Divisor.
         */
        private final transient int divisor;
        /**
         * Protected ctor.
         * @param div The divisor
         */
        protected ModuloGate(final int div) {
            Validate.isTrue(
                div > 0, "divisor %d has to be positive and non-zero",
                div
            );
            this.divisor = div;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("*/%d", this.divisor);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean pass(final Integer num) {
            return num % this.divisor == 0;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long lag(final Integer num) {
            long lag = 0;
            if (!this.pass(num)) {
                lag = ((num + this.divisor - 1)
                    / this.divisor) * this.divisor - num;
            }
            return lag;
        }
    }

    /**
     * Parse text into alternative.
     * @param part The text to parse
     * @return Small gate condition
     */
    private static Crontab.Gate<Integer> parse(final String part) {
        final Crontab.Gate<Integer> alternative;
        if (part.matches("\\d+")) {
            alternative = new Crontab.ExactGate(Integer.parseInt(part));
        } else if (part.matches("\\d+-\\d+")) {
            final String[] numbers = part.split("-");
            alternative = new Crontab.IntervalGate(
                Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1])
            );
        } else if (part.matches("\\*/\\d+")) {
            alternative = new Crontab.ModuloGate(
                Integer.valueOf(part.substring(part.indexOf('/') + 1))
            );
        // @checkstyle MultipleStringLiterals (1 line)
        } else if ("*".equals(part)) {
            alternative = new Crontab.Gate<Integer>() {
                @Override
                public String toString() {
                    return "*";
                }
                @Override
                public boolean pass(final Integer num) {
                    return true;
                }
                @Override
                public long lag(final Integer num) {
                    return 0L;
                }
            };
        } else {
            throw new IllegalArgumentException(
                String.format("invalid crontab sector `%s`", part)
            );
        }
        return alternative;
    }

    /**
     * Split into parts.
     * @param text Text to split
     * @return Five parts, numbers
     */
    @SuppressWarnings("unchecked")
    private static Crontab.Gate<Calendar>[] split(final String text) {
        String src = text;
        if (Crontab.DEFS.containsKey(src)) {
            src = Crontab.DEFS.get(src);
        }
        final String[] parts = src.split("\\s+");
        if (parts.length != Tv.FIVE) {
            throw new IllegalArgumentException(
                String.format("invalid crontab definition `%s`", text)
            );
        }
        return (Crontab.Gate<Calendar>[]) new Crontab.Gate<?>[] {
            new Crontab.AbstractBigGate(parts[0]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.MINUTE));
                }
                @Override
                public long lag(final Calendar calendar) {
                    return this.lag(
                        calendar.get(Calendar.MINUTE)
                    ) * TimeUnit.MINUTES.toMillis(1);
                }
            },
            new Crontab.AbstractBigGate(parts[1]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.HOUR_OF_DAY));
                }
                @Override
                public long lag(final Calendar calendar) {
                    return this.lag(
                        calendar.get(Calendar.HOUR_OF_DAY)
                    ) * TimeUnit.HOURS.toMillis(1);
                }
            },
            new Crontab.AbstractBigGate(parts[2]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(
                        calendar.get(Calendar.DAY_OF_MONTH)
                    );
                }
                @Override
                public long lag(final Calendar calendar) {
                    return this.lag(
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ) * TimeUnit.DAYS.toMillis(1);
                }
            },
            new Crontab.AbstractBigGate(parts[Tv.THREE]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.MONTH) + 1);
                }
                @Override
                public long lag(final Calendar calendar) {
                    return this.lag(
                        calendar.get(Calendar.MONTH) + 1
                    ) * Tv.THIRTY * TimeUnit.DAYS.toMillis(1);
                }
            },
            new Crontab.AbstractBigGate(parts[Tv.FOUR]) {
                @Override
                public boolean pass(final Calendar calendar) {
                    return this.matches(calendar.get(Calendar.DAY_OF_WEEK));
                }
                @Override
                public long lag(final Calendar calendar) {
                    return this.lag(
                        calendar.get(Calendar.DAY_OF_WEEK) - 1
                    ) * TimeUnit.DAYS.toMillis(1);
                }
            },
        };
    }

    /**
     * Convert date to calendar.
     * @param date Current date
     * @return Calendar or today
     */
    private static Calendar calendar(final Time date) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date.date());
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        return cal;
    }

    /**
     * Convert time into text.
     * @param date The date
     * @return Text in crontab format
     */
    private static String moment(final Time date) {
        final Calendar cal = Crontab.calendar(date);
        return String.format(
            "%d %d %d %d %d",
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_WEEK) - 1
        );
    }

}
