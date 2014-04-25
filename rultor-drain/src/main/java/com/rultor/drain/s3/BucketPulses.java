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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.S3Client;
import com.rultor.spi.Pageable;
import com.rultor.tools.Time;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Pageable in bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "prefix", "top", "client" })
@Loggable(Loggable.DEBUG)
@ToString
final class BucketPulses implements Pageable<Time, Time> {

    /**
     * The prefix.
     */
    private final transient String prefix;

    /**
     * The tail of the vector happened above this one (this time should not
     * be visible in the iterator).
     */
    private final transient Time top;

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Public ctor.
     * @param clnt S3 client
     * @param pfx Prefix
     * @param time Tail of the previous vector (is not visible in this iterator)
     */
    protected BucketPulses(final S3Client clnt, final String pfx,
        final Time time) {
        this.client = clnt;
        this.prefix = pfx;
        this.top = time;
    }

    @Override
    public Pageable<Time, Time> tail(final Time head) {
        return new BucketPulses(
            this.client, this.prefix, new Time(head.millis() + 1)
        );
    }

    @Override
    public Iterator<Time> iterator() {
        final String mrkr = new StringBuilder(this.prefix)
            .append(new Key(this.top)).toString();
        // @checkstyle AnonInnerLength (100 lines)
        return new Iterator<Time>() {
            private final transient Queue<Time> queue = new LinkedList<Time>();
            private transient String marker = mrkr;
            @Override
            public boolean hasNext() {
                if (this.queue.isEmpty() && this.marker != null) {
                    this.fetch();
                }
                return !this.queue.isEmpty();
            }
            @Override
            public Time next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return this.queue.poll();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            private void fetch() {
                final AmazonS3 aws = BucketPulses.this.client.get();
                final ListObjectsRequest request = new ListObjectsRequest()
                    .withBucketName(BucketPulses.this.client.bucket())
                    .withMarker(this.marker)
                    .withMaxKeys(Tv.TEN)
                    .withPrefix(BucketPulses.this.prefix);
                final ObjectListing listing = aws.listObjects(request);
                for (final S3ObjectSummary sum : listing.getObjectSummaries()) {
                    this.queue.add(
                        Key.valueOf(
                            sum.getKey().substring(
                                BucketPulses.this.prefix.length()
                            )
                        ).time()
                    );
                }
                Logger.info(
                    this,
                    // @checkstyle LineLength (1 line)
                    "loaded %d pulse(s) from S3 bucket `%s`, marker is `%s`, next marker is `%s`",
                    this.queue.size(),
                    listing.getBucketName(),
                    this.marker,
                    listing.getNextMarker()
                );
                this.marker = listing.getNextMarker();
            }
        };
    }

}
