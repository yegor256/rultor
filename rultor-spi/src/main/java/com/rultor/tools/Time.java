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
package com.rultor.tools;

import com.jcabi.aspects.Immutable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Time.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "msec")
public final class Time implements Comparable<Time> {

    /**
     * ISO 8601.
     */
    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Milliseconds.
     */
    private final transient long msec;

    /**
     * Public ctor (current time).
     */
    public Time() {
        this(System.currentTimeMillis());
    }

    /**
     * Public ctor.
     * @param millis Milliseconds
     */
    public Time(final long millis) {
        Validate.isTrue(millis >= 0, "millis can't be negative: %d", millis);
        this.msec = millis;
    }

    /**
     * Public ctor.
     * @param date Date
     */
    public Time(@NotNull(message = "date can't be NULL") final Date date) {
        this(date.getTime());
    }

    /**
     * Public ctor.
     * @param date Date
     */
    public Time(@NotNull(message = "text can't be NULL") final String date) {
        this(Time.parse(date));
    }

    @Override
    public String toString() {
        return DateFormatUtils.formatUTC(this.msec, Time.FORMAT);
    }

    @Override
    public int compareTo(final Time time) {
        final int cmp;
        if (this.msec < time.msec) {
            cmp = -1;
        } else if (this.msec > time.msec) {
            cmp = 1;
        } else {
            cmp = 0;
        }
        return cmp;
    }

    /**
     * Calculate distance in milliseconds.
     * @param time Time to reach
     * @return Distance between this time and given one
     */
    public long delta(@NotNull(message = "delta time can't be NULL")
        final Time time) {
        return this.msec - time.msec;
    }

    /**
     * Get milliseconds.
     * @return Milliseconds
     */
    public long millis() {
        return this.msec;
    }

    /**
     * Convert it to date.
     * @return Date
     */
    public Date date() {
        return new Date(this.msec);
    }

    /**
     * Get rid of seconds and milliseconds.
     * @return New time without seconds and milliseconds
     */
    public Time round() {
        return new Time(DateUtils.truncate(this.date(), Calendar.MINUTE));
    }

    /**
     * Parse ISO 8601.
     * @param text Text to pars
     * @return Milliseconds
     */
    private static long parse(final String text) {
        final SimpleDateFormat fmt = new SimpleDateFormat(
            Time.FORMAT, Locale.ENGLISH
        );
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        final long time;
        try {
            time = fmt.parse(text).getTime();
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
        assert time > 0 : "can't be negative";
        return time;
    }

}
