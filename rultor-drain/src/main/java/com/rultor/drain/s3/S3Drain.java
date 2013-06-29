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
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.io.CountingInputStream;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.S3Client;
import com.rultor.spi.Drain;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Drain in S3 objects.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "client")
@Loggable(Loggable.DEBUG)
public final class S3Drain implements Drain {

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * S3 objects prefix.
     */
    private final transient String prefix;

    /**
     * Public ctor.
     * @param clnt S3 client
     * @param pfx S3 object prefix
     */
    public S3Drain(@NotNull final S3Client clnt, @NotNull final String pfx) {
        this.client = clnt;
        Validate.matchesPattern(pfx, "([^/]+/)+");
        this.prefix = pfx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "`%s` at %s",
            this.prefix,
            this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> pulses() {
        final SortedSet<Long> numbers =
            new TreeSet<Long>(Collections.reverseOrder());
        final AmazonS3 aws = this.client.get();
        final ListObjectsRequest request = new ListObjectsRequest()
            .withBucketName(this.client.bucket())
            .withMaxKeys(Tv.TEN)
            .withPrefix(this.prefix);
        final ObjectListing listing = aws.listObjects(request);
        for (S3ObjectSummary sum : listing.getObjectSummaries()) {
            numbers.add(
                Key.valueOf(
                    sum.getKey().substring(this.prefix.length())
                ).date().getTime()
            );
        }
        return Collections.unmodifiableSortedSet(numbers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final long date, final Iterable<String> lines)
        throws IOException {
        final String key = this.key(date);
        final AmazonS3 aws = this.client.get();
        final CountingInputStream stream = new CountingInputStream(
            new SequenceInputStream(
                this.read(date),
                IOUtils.toInputStream(
                    StringUtils.join(lines, "\n"), CharEncoding.UTF_8
                )
            )
        );
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentEncoding(CharEncoding.UTF_8);
        meta.setContentType(MediaType.TEXT_PLAIN);
        try {
            final PutObjectResult result = aws.putObject(
                this.client.bucket(),
                key,
                stream,
                meta
            );
            Logger.info(
                this,
                "'%s' saved %d byte(s) to S3, etag=%s",
                key,
                stream.getCount(),
                result.getETag()
            );
        } catch (AmazonS3Exception ex) {
            throw new IOException(
                String.format(
                    "failed to flush %s to %s: %s",
                    key,
                    this.client.bucket(),
                    ex
                ),
                ex
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read(final long date) throws IOException {
        final String key = this.key(date);
        final AmazonS3 aws = this.client.get();
        InputStream stream;
        try {
            if (aws.listObjects(this.client.bucket(), key)
                .getObjectSummaries().isEmpty()) {
                stream = IOUtils.toInputStream("");
            } else {
                final S3Object object =
                    aws.getObject(this.client.bucket(), key);
                Logger.info(
                    this,
                    "'%s' ready for loading from S3, size=%d, etag=%s",
                    key,
                    object.getObjectMetadata().getContentLength(),
                    object.getObjectMetadata().getETag()
                );
                stream = object.getObjectContent();
            }
        } catch (AmazonS3Exception ex) {
            throw new IOException(
                String.format(
                    "failed to read %s from %s: %s",
                    key,
                    this.client.bucket(),
                    ex
                ),
                ex
            );
        }
        return stream;
    }

    /**
     * Make a key.
     * @param date The date
     * @return S3 object key
     */
    private String key(final long date) {
        return String.format("%s%s", this.prefix, new Key(date));
    }

}
