/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;
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
        final String date = "2005-10-08T15:48:39Z";
        Assertions.assertDoesNotThrow(
            () -> new Time(date),
            "Time should be able to create from date-time string"
        );
    }

    /**
     * ISO value can be parsed independently of the default time zone.
     */
    @Test
    void roundTripsIsoOutsideUtc() {
        final TimeZone origin = TimeZone.getDefault();
        final long millis = Instant.parse("2005-10-08T15:48:39Z").toEpochMilli();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
            MatcherAssert.assertThat(
                "ISO round-trip should preserve the instant",
                new Time(new Time(millis).iso()).msec(),
                Matchers.equalTo(millis)
            );
        } finally {
            TimeZone.setDefault(origin);
        }
    }

    /**
     * Date can not be parsed from invalid string.
     * @param date Date to check
     */
    @ParameterizedTest
    @ValueSource(
        strings = {
            "2005-10-08T15:48:39",
            "2005-10-0815:48:28",
            "2005-10-08",
            "15:48:28"
        }
    )
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
                DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US
                ).withZone(ZoneOffset.UTC).format(instant)
            )
        );
    }

    /**
     * Test that default value is now.
     */
    @Test
    void defaultNowTime() {
        final Instant instant = Instant.now();
        MatcherAssert.assertThat(
            "Time without parameters should get current time",
            new Time().msec(),
            Matchers.allOf(
                Matchers.greaterThanOrEqualTo(instant.toEpochMilli()),
                Matchers.lessThan(instant.toEpochMilli() + 5)
            )
        );
    }

    /**
     * Time can be created from Instant.
     */
    @Test
    void fromInstantValidTime() {
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
        final Instant instant = Instant.now();
        MatcherAssert.assertThat(
            "Time should get msec value from parameter",
            new Time(instant.toEpochMilli()).msec(),
            Matchers.equalTo(instant.toEpochMilli())
        );
    }
}
