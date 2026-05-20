/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import com.jcabi.aspects.Immutable;
import java.time.Instant;
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
     * ISO format with trailing Z for output.
     */
    private static final DateTimeFormatter ISO_OUT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        .withZone(ZoneOffset.UTC);

    /**
     * ISO format for input parsing.
     */
    private static final DateTimeFormatter ISO_IN = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        .withZone(ZoneOffset.UTC);

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
        return Time.ISO_OUT.format(Instant.ofEpochMilli(this.millis));
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
     * @return Epoch millis
     */
    private static long parse(final String date) {
        try {
            return Instant.from(Time.ISO_IN.parse(date)).toEpochMilli();
        } catch (final DateTimeParseException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
