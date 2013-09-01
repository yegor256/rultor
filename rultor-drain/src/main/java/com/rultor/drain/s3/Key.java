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
package com.rultor.drain.s3;

import com.jcabi.aspects.Loggable;
import com.rultor.tools.Time;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

/**
 * S3 key.
 *
 * <p>Every log is stored as a plain text object in Amazon S3, named as
 * <code>owner/rule/year/month/day/uid.txt</code>, where all
 * time values are in numbers. For example:
 * <code>urn:facebook:5463/nighly-build/8987/88/74/7843.txt</code>. In this
 * example: 8987 is year 2013, reverted towards 9999, 88 is December (99 minus
 * 11), 74 is 25 (99 minus 25), and 7843 is Long.MAX_VALUE minus millisTime
 * of pulse start. Such a reverted mechanism is required in order to
 * utilize native sorting provided by Amazon S3.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@EqualsAndHashCode(of = "when")
@Loggable(Loggable.DEBUG)
final class Key implements Comparable<Key> {

    /**
     * Pattern to parse.
     */
    private static final Pattern PATTERN = Pattern.compile(
        "\\d{4}/\\d{2}/\\d{2}/(\\d+)\\.txt"
    );

    /**
     * Date.
     */
    private final transient Time when;

    /**
     * Private ctor.
     * @param date Date
     */
    protected Key(final Time date) {
        this.when = date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(this.when.millis());
        return String.format(
            "%d/%d/%d/%s.txt",
            // @checkstyle MagicNumber (3 lines)
            9999 - cal.get(Calendar.YEAR),
            99 - cal.get(Calendar.MONTH),
            99 - cal.get(Calendar.DAY_OF_MONTH),
            Long.MAX_VALUE - this.when.millis()
        );
    }

    /**
     * Parse it back.
     * @param text The text to parse
     * @return Key found
     */
    public static Key valueOf(final String text) {
        final Matcher matcher = Key.PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format("invalid key '%s'", text)
            );
        }
        return new Key(
            new Time(Long.MAX_VALUE - Long.parseLong(matcher.group(1)))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Key key) {
        return key.when.compareTo(this.when);
    }

    /**
     * Get time.
     * @return Time
     */
    public Time time() {
        return this.when;
    }

}
