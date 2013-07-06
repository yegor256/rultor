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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.aws.S3Client;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulses;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Drain in a single S3 object.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "client", "key" })
@Loggable(Loggable.DEBUG)
public final class ObjectDrain implements Drain {

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * S3 object name.
     */
    private final transient String key;

    /**
     * Public ctor.
     * @param clnt S3 client
     * @param name S3 object name
     */
    public ObjectDrain(@NotNull final S3Client clnt,
        @NotNull final String name) {
        this.client = clnt;
        Validate.matchesPattern(
            name, "([^/]+/)*[^/]+", "invalid S3 object name '%s'", name
        );
        this.key = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "`%s` at %s",
            this.key,
            this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pulses pulses() {
        return new Pulses.Array();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        final AmazonS3 aws = this.client.get();
        final InputStream body;
        long size;
        if (aws.listObjects(this.client.bucket(), this.key)
            .getObjectSummaries().isEmpty()) {
            body = IOUtils.toInputStream("");
            size = 0;
        } else {
            final S3Object object =
                aws.getObject(this.client.bucket(), this.key);
            body = object.getObjectContent();
            size = object.getObjectMetadata().getContentLength();
        }
        final byte[] suffix = String.format(
            "%s\n",
            StringUtils.join(lines, "\n")
        ).getBytes(CharEncoding.UTF_8);
        Validate.isTrue(suffix.length > 0, "empty input");
        size += suffix.length;
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentEncoding(CharEncoding.UTF_8);
        meta.setContentType(MediaType.TEXT_PLAIN);
        meta.setContentLength(size);
        try {
            final PutObjectResult result = aws.putObject(
                this.client.bucket(),
                this.key,
                new SequenceInputStream(
                    body,
                    new ByteArrayInputStream(suffix)
                ),
                meta
            );
            Logger.info(
                this,
                "'%s' saved %d byte(s) to S3, etag=%s",
                this.key,
                size,
                result.getETag()
            );
        } catch (AmazonS3Exception ex) {
            throw new IOException(
                String.format(
                    "failed to flush %s to %s: %s",
                    this.key,
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
    public InputStream read() throws IOException {
        final AmazonS3 aws = this.client.get();
        InputStream stream;
        try {
            if (aws.listObjects(this.client.bucket(), this.key)
                .getObjectSummaries().isEmpty()) {
                throw new IOException(
                    String.format(
                        "S3 object %s not found in %s",
                        this.key,
                        this.client.bucket()
                    )
                );
            }
            final S3Object object =
                aws.getObject(this.client.bucket(), this.key);
            Logger.info(
                this,
                "'%s' ready for loading from S3, size=%d, etag=%s",
                this.key,
                object.getObjectMetadata().getContentLength(),
                object.getObjectMetadata().getETag()
            );
            stream = new SequenceInputStream(
                IOUtils.toInputStream(
                    String.format(
                        "ObjectDrain: etag='%s', size=%d\n",
                        object.getObjectMetadata().getETag(),
                        object.getObjectMetadata().getContentLength()
                    )
                ),
                new ObjectDrain.Wrap(object.getObjectContent(), aws)
            );
        } catch (AmazonS3Exception ex) {
            throw new IOException(
                String.format(
                    "failed to read %s from %s: %s",
                    this.key,
                    this.client.bucket(),
                    ex
                ),
                ex
            );
        }
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "ObjectDrain: key='%s', client='%s'\n",
                    this.key,
                    this.client
                )
            ),
            stream
        );
    }

    /**
     * Wrap around exiting InputStream, to keep AWS object apart from
     * garbage collector, in order not to loose HTTP connection.
     */
    private static final class Wrap extends BufferedInputStream {
        /**
         * Encapsulated AmazonS3 client.
         */
        private final transient AmazonS3 aws;
        /**
         * Public ctor.
         * @param stream Origin input stream
         * @param amazon AmazonS3
         */
        protected Wrap(final InputStream stream, final AmazonS3 amazon) {
            super(stream);
            this.aws = amazon;
        }
    }

}
