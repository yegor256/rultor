/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor;

import java.text.SimpleDateFormat;
import java.util.Date;
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
 *
 * @since 2.0
 */
final class TimeTest {
    /**
     * Date can be parsed from string.
     */
    @Test
    void canParseValidTime() {
        final String date = "2005-10-08T15:48:39";
        Assertions.assertDoesNotThrow(
            () -> new Time(date),
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
        final Date date = new Date();
        final SimpleDateFormat format =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        final Time time =  new Time(date);
        MatcherAssert.assertThat(
            "ISO value should be for the GMT timezone",
            time.iso(),
            Matchers.equalTo(format.format(date))
        );
    }

    /**
     * Test that default value is now.
     */
    @Test
    void defaultNowTime() {
        final Date date = new Date();
        final Time time = new Time();
        MatcherAssert.assertThat(
            "Time without parameters should get current time",
            time.msec(),
            Matchers.allOf(
                Matchers.greaterThanOrEqualTo(date.getTime()),
                Matchers.lessThan(date.getTime() + 5)
            )
        );
    }

    /**
     * Time can be created from Date.
     */
    @Test
    void fromDateValidTime() {
        final Date date = new Date();
        final Time time = new Time(date);
        MatcherAssert.assertThat(
            "Time should get date from the parameter",
            time.msec(),
            Matchers.equalTo(date.getTime())
        );
    }

    /**
     * Time can be created from ms value.
     */
    @Test
    void fromMsValidTime() {
        final Date date = new Date();
        final Time time = new Time(date.getTime());
        MatcherAssert.assertThat(
            "Time should get msec value from parameter",
            time.msec(),
            Matchers.equalTo(date.getTime())
        );
    }
}
