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
import com.rultor.spi.Pulseable;
import com.rultor.spi.State;
import com.rultor.spi.Work;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Sends pulses through, only on certain time moments.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @see <a href="https://en.wikipedia.org/wiki/Cron">Cron in Wikipedia</a>
 * @see <a href="http://pubs.opengroup.org/onlinepubs/9699919799/utilities/crontab.html">Crontab specification</a>
 */
@Immutable
@ToString
@EqualsAndHashCode(
    of = { "origin", "minute", "hour", "day", "month", "weekday" }
)
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.NPathComplexity", "PMD.CyclomaticComplexity" })
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
     * Minute.
     */
    private final transient int minute;

    /**
     * Hour.
     */
    private final transient int hour;

    /**
     * Day.
     */
    private final transient int day;

    /**
     * Month.
     */
    private final transient int month;

    /**
     * Weekday.
     */
    private final transient int weekday;

    /**
     * Public ctor.
     * @param text Mask to use
     * @param pls Original pulseable
     */
    public Crontab(final String text, final Pulseable pls) {
        this.origin = pls;
        final int[] parts = Crontab.split(text);
        this.minute = parts[0];
        this.hour = parts[1];
        this.day = parts[2];
        this.month = parts[Tv.THREE] - 1;
        this.weekday = parts[Tv.FOUR];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pulse(@NotNull final Work work, @NotNull final State state)
        throws Exception {
        final String key = DateFormatUtils.formatUTC(
            this.next(), "yyyy-MM-dd'T'HH:mm'Z'"
        );
        if (state.checkAndSet(key, "passed")) {
            this.origin.pulse(work, state);
        }
    }

    /**
     * Make next execution date.
     * @return Date when we should execute next
     * @checkstyle CyclomaticComplexity (50 lines)
     * @checkstyle NPathComplexity (50 lines)
     */
    private Date next() {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (this.month >= 0) {
            if (cal.get(Calendar.MONTH) > this.month) {
                cal.add(Calendar.YEAR, -1);
            }
            cal.set(Calendar.MONTH, this.month);
        }
        if (this.day >= 0) {
            if (cal.get(Calendar.DAY_OF_MONTH) > this.day) {
                cal.add(Calendar.MONTH, -1);
            }
            cal.set(Calendar.DAY_OF_MONTH, this.day);
        }
        if (this.hour >= 0) {
            if (cal.get(Calendar.HOUR_OF_DAY) > this.hour) {
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
            cal.set(Calendar.HOUR_OF_DAY, this.hour);
        }
        if (this.minute >= 0) {
            if (cal.get(Calendar.MINUTE) > this.minute) {
                cal.add(Calendar.HOUR_OF_DAY, -1);
            }
            cal.set(Calendar.MINUTE, this.minute);
        }
        if (this.weekday >= 0) {
            if (cal.get(Calendar.DAY_OF_WEEK) > this.weekday) {
                cal.add(Calendar.WEEK_OF_YEAR, -1);
            }
            cal.set(Calendar.DAY_OF_WEEK, this.weekday);
        }
        return cal.getTime();
    }

    /**
     * Split into parts.
     * @param text Text to split
     * @return Five parts, numbers
     */
    private static int[] split(final String text) {
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
        final int[] numbers = new int[parts.length];
        for (int idx = 0; idx < parts.length; ++idx) {
            if ("*".equals(parts[idx])) {
                numbers[idx] = -1;
            } else if (parts[idx].matches("\\d+")) {
                numbers[idx] = Integer.parseInt(parts[idx]);
            } else {
                throw new IllegalArgumentException(
                    String.format(
                        "invalid crontab part #%d in '%s'",
                        idx,
                        parts[idx]
                    )
                );
            }
        }
        return numbers;
    }

}
