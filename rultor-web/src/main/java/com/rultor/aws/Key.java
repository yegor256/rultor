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
package com.rultor.aws;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.spi.Work;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

/**
 * S3 key.
 *
 * <p>Every log is stored as a plain text object in Amazon S3, named as
 * <code>owner/unit/year/month/day/uid.txt</code>, where all
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
@EqualsAndHashCode(of = { "owner", "unit", "when" })
@Loggable(Loggable.DEBUG)
final class Key implements Comparable<Key> {

    /**
     * Pattern to parse.
     */
    private static final Pattern PATTERN = Pattern.compile(
        "(urn:[a-z]+:\\d+)/([\\-\\w]+)/\\d{4}/\\d{2}/\\d{2}/(\\d+)\\.txt"
    );

    /**
     * S3 client.
     */
    private final transient S3Client clnt;

    /**
     * Owner.
     */
    private final transient URN owner;

    /**
     * Unit.
     */
    private final transient String unit;

    /**
     * Date.
     */
    private final transient long when;

    /**
     * Public ctor.
     * @param client S3 client
     * @param work The work
     */
    protected Key(final S3Client client, final Work work) {
        this(client, work.owner(), work.unit(), work.started());
    }

    /**
     * Private ctor.
     * @param client S3 client
     * @param urn Owner
     * @param name Unit name
     * @param date Date
     * @checkstyle ParameterNumber (4 lines)
     */
    protected Key(final S3Client client, final URN urn,
        final String name, final long date) {
        this.clnt = client;
        this.owner = urn;
        this.unit = name;
        this.when = date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(this.when);
        return String.format(
            "%s/%s/%d/%d/%d/%s.txt",
            this.owner,
            this.unit,
            // @checkstyle MagicNumber (3 lines)
            9999 - cal.get(Calendar.YEAR),
            99 - cal.get(Calendar.MONTH),
            99 - cal.get(Calendar.DAY_OF_MONTH),
            Long.MAX_VALUE - this.when
        );
    }

    /**
     * Parse it back.
     * @param client S3 client
     * @param text The text to parse
     * @return Key found
     */
    public static Key valueOf(final S3Client client, final String text) {
        final Matcher matcher = Key.PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format("invalid key '%s'", text)
            );
        }
        return new Key(
            client,
            URN.create(matcher.group(1)),
            matcher.group(2),
            Long.MAX_VALUE - Long.parseLong(matcher.group(Tv.THREE))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Key key) {
        return Long.compare(key.when, this.when);
    }

    /**
     * Get client.
     * @return S3 client
     */
    public S3Client client() {
        return this.clnt;
    }

    /**
     * Get date.
     * @return S3 client
     */
    public Date date() {
        return new Date(this.when);
    }

    /**
     * Belongs to this user/unit?
     * @param urn URN of the owner
     * @param name Name of the unit
     * @return TRUE if belongs
     */
    public boolean belongsTo(final URN urn, final String name) {
        return this.owner.equals(urn) && this.unit.equals(name);
    }

}
