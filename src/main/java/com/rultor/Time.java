/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import com.jcabi.aspects.Immutable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Date and time in ISO 8601.
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
     * @param instant Instant
     */
    public Time(final Instant instant) {
        this(instant.toEpochMilli());
    }

    /**
     * Ctor.
     * @param date Date
     */
    public Time(final String date) {
        this(Time.parse(date));
    }

    /**
     * Ctor.
     * @param msec Milliseconds
     */
    public Time(final long msec) {
        this.millis = msec;
    }

    /**
     * Make ISO string.
     * @return Text
     */
    public String iso() {
        return DateTimeFormatter.ofPattern(
            "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US
        ).withZone(ZoneOffset.UTC).format(Instant.ofEpochMilli(this.millis));
    }

    /**
     * Make date.
     * @return Date
     */
    public long msec() {
        return this.millis;
    }

    /**
     * Parse text, produced by {@link #iso()} or in a similar format.
     * @param date Date
     * @return Instant
     */
    private static Instant parse(final String date) {
        final String txt;
        if (date.endsWith("Z")) {
            txt = date.substring(0, date.length() - 1);
        } else {
            txt = date;
        }
        try {
            return LocalDateTime.parse(
                txt,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            ).atZone(ZoneId.systemDefault()).toInstant();
        } catch (final DateTimeParseException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
