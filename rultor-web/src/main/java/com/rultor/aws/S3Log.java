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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.rultor.spi.Conveyer;
import com.rultor.spi.Pulse;
import com.rultor.spi.Work;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Log files in Amazon S3.
 *
 * <p>Every log is stored as a plain text object in Amazon S3, named as
 * <code>owner/unit/year/month/day/uid.txt</code>, where all
 * time values are in numbers. For example:
 * <code>urn:facebook:5463/nighly-build/8987/88/74/7843.txt</code>. In this
 * example: 8987 is year 2013, reverted towards 9999, 88 is December (99 minus
 * 11), 74 is 25 (99 minus 25), and 7843 is Long.MAX_VALUE minus millisTime
 * of pulse start.
 *
 * <p>Every TXT object in S3 contains a JSON meta data in the first line,
 * and the rest of file contains plain text log lines.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(of = { "client", "bucket" })
@Loggable(Loggable.DEBUG)
public final class S3Log implements Conveyer.Log, Flushable, Closeable {

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Bucket unit.
     */
    private final transient String bucket;

    /**
     * All cached objects.
     */
    private final transient Caches caches;

    /**
     * Public ctor.
     * @param key AWS key
     * @param secret AWS secret
     * @param bkt Bucket unit
     */
    public S3Log(final String key, final String secret, final String bkt) {
        this(new S3Client.Simple(key, secret), bkt);
        Logger.info(S3Log.class, "S3Log with key=%s, bucket=%s", key, bkt);
    }

    /**
     * Public ctor.
     * @param clnt Client
     * @param bkt Bucket unit
     */
    protected S3Log(final S3Client clnt, final String bkt) {
        this.client = clnt;
        this.bucket = bkt;
        this.caches = new Caches(this.client, this.bucket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(final Work work, final Conveyer.Line line) {
        try {
            this.caches.get(new Key(work)).append(
                String.format(
                    "%tM:%<tS %5s %s %s",
                    new Date(),
                    line.level(),
                    line.logger(),
                    line.message()
                )
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get pulses.
     * @param owner Owner
     * @param unit Unit unit
     * @return All pulses of this unit
     */
    public List<Pulse> pulses(final URN owner, final String unit) {
        final List<Pulse> pulses = new LinkedList<Pulse>();
        final Collection<Key> keys = new TreeSet<Key>();
        keys.addAll(this.fetch(owner, unit));
        keys.addAll(this.caches.keys(owner, unit));
        for (Key key : keys) {
            pulses.add(new S3Pulse(this.caches, key));
        }
        return pulses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        this.caches.flush();
    }

    /**
     * Get all keys from S3.
     * @param owner Owner
     * @param unit Unit unit
     * @return All keys
     */
    private Collection<Key> fetch(final URN owner, final String unit) {
        final AmazonS3 aws = this.client.get();
        final ListObjectsRequest request = new ListObjectsRequest()
            .withBucketName(this.bucket)
            .withMaxKeys(Tv.TWENTY)
            .withPrefix(String.format("%s/%s/", owner, unit));
        final ObjectListing listing = aws.listObjects(request);
        final Collection<Key> keys = new LinkedList<Key>();
        for (S3ObjectSummary sum : listing.getObjectSummaries()) {
            keys.add(Key.valueOf(sum.getKey()));
        }
        return keys;
    }

}
