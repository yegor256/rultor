/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link Time}.
 * @since 2.0
 */
final class TimeTest {

    /**
     * Date can be parsed from string.
     */
    @Test
    void canParseValidTime() {
        Assertions.assertDoesNotThrow(
            () -> new Time("2005-10-08T15:48:39"),
            "Time should be able to create from date-time string"
        );
    }

    /**
     * Date can not be parsed from invalid string.
     * @param date Date to check
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "2005-10-0815:48:28",
        "2005-10-08",
        "15:48:28"
    })
    void exceptionParseInvalidTime(final String date) {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Time(date),
            "Exception is expected for invalid date time string"
        );
    }

    /**
     * Check that iso format is correct.
     */
    @Test
    void isoValidFormat() {
        final Instant instant = Instant.now();
        MatcherAssert.assertThat(
            "ISO value should be for the GMT timezone",
            new Time(instant).iso(),
            Matchers.equalTo(
                DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .withZone(ZoneOffset.UTC)
                    .format(instant)
            )
        );
    }

    /**
     * Test that default value is now.
     */
    @Test
    void defaultNowTime() {
        final long now = System.currentTimeMillis();
        MatcherAssert.assertThat(
            "Time without parameters should get current time",
            new Time().msec(),
            Matchers.allOf(
                Matchers.greaterThanOrEqualTo(now),
                Matchers.lessThan(now + 5)
            )
        );
    }

    /**
     * Time can be created from Instant.
     */
    @Test
    void fromDateValidTime() {
        final Instant instant = Instant.now();
        MatcherAssert.assertThat(
            "Time should get date from the parameter",
            new Time(instant).msec(),
            Matchers.equalTo(instant.toEpochMilli())
        );
    }

    /**
     * Time can be created from ms value.
     */
    @Test
    void fromMsValidTime() {
        final long now = System.currentTimeMillis();
        MatcherAssert.assertThat(
            "Time should get msec value from parameter",
            new Time(now).msec(),
            Matchers.equalTo(now)
        );
    }
}
