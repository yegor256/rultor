/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import com.jcabi.aspects.Immutable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Date and time in ISO 8601.
 *
 * @since 1.8.12
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "millis")
public final class Time {

    /**
     * The time.
     */
    private final transient long millis;

    /**
     * Ctor.
     */
    public Time() {
        this(System.currentTimeMillis());
    }

    /**
     * Ctor.
     * @param date Date
     */
    public Time(final Date date) {
        this(date.getTime());
    }

    /**
     * Ctor.
     * @param msec Milliseconds
     */
    public Time(final long msec) {
        this.millis = msec;
    }

    /**
     * Ctor.
     * @param date Date
     */
    public Time(final String date) {
        this(Time.parse(date));
    }

    /**
     * Make ISO string.
     * @return Text
     */
    public String iso() {
        final SimpleDateFormat format =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(new Date(this.millis));
    }

    /**
     * Make date.
     * @return Date
     */
    public long msec() {
        return this.millis;
    }

    /**
     * Parse text.
     * @param date Date
     * @return Date
     */
    private static Date parse(final String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                .parse(date);
        } catch (final ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
