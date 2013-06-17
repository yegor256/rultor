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
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.spi.Conveyer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;

/**
 * Mutable and thread-safe in-memory cache of a single S3 object.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(of = "key")
@Loggable(Loggable.DEBUG)
final class Cache implements Flushable {

    /**
     * S3 key.
     */
    private final transient Key key;

    /**
     * When was is touched last time (in milliseconds)?
     */
    private final transient AtomicLong touched =
        new AtomicLong(System.currentTimeMillis());

    /**
     * Is it dirty and needs flushing?
     */
    private final transient AtomicBoolean dirty = new AtomicBoolean();

    /**
     * Data.
     */
    private transient ByteArrayOutputStream data;

    /**
     * Public ctor.
     * @param akey S3 key
     */
    protected Cache(final Key akey) {
        this.key = akey;
    }

    /**
     * Append new line to an object.
     * @param line Line to add
     * @throws IOException If fails
     */
    public void append(final Conveyer.Line line) throws IOException {
        synchronized (this.key) {
            final PrintWriter writer = new PrintWriter(this.stream());
            writer.append(line.toString()).append(CharUtils.LF);
            writer.flush();
            this.dirty.set(true);
        }
    }

    /**
     * Read the entire object.
     * @return Input stream
     * @throws IOException If fails
     */
    public InputStream read() throws IOException {
        return new ByteArrayInputStream(this.stream().toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        synchronized (this.key) {
            if (this.data != null && this.dirty.get()) {
                final S3Client client = this.key.client();
                final AmazonS3 aws = client.get();
                final ObjectMetadata meta = new ObjectMetadata();
                meta.setContentEncoding(CharEncoding.UTF_8);
                meta.setContentLength(this.data.size());
                meta.setContentType(MediaType.TEXT_PLAIN);
                try {
                    if (this.valuable()) {
                        final PutObjectResult result = aws.putObject(
                            client.bucket(),
                            this.key.toString(),
                            new ByteArrayInputStream(this.data.toByteArray()),
                            meta
                        );
                        Logger.info(
                            this,
                            "'%s' saved to S3, size=%d, etag=%s",
                            this.key,
                            this.data.size(),
                            result.getETag()
                        );
                    }
                } catch (AmazonS3Exception ex) {
                    throw new IOException(
                        String.format(
                            "failed to flush %s to %s: %s",
                            this.key,
                            client.bucket(),
                            ex
                        ),
                        ex
                    );
                }
                this.dirty.set(false);
            }
        }
    }

    /**
     * Get stream.
     * @return Stream with data
     * @throws IOException If fails
     */
    private ByteArrayOutputStream stream() throws IOException {
        synchronized (this.key) {
            if (this.data == null) {
                this.data = new ByteArrayOutputStream();
                final S3Client client = this.key.client();
                final AmazonS3 aws = client.get();
                try {
                    if (!aws.listObjects(client.bucket(), this.key.toString())
                        .getObjectSummaries().isEmpty()) {
                        final S3Object object =
                            aws.getObject(client.bucket(), this.key.toString());
                        IOUtils.copy(object.getObjectContent(), this.data);
                        Logger.info(
                            this,
                            "'%s' loaded from S3, size=%d, etag=%s",
                            this.key,
                            this.data.size(),
                            object.getObjectMetadata().getETag()
                        );
                    }
                } catch (AmazonS3Exception ex) {
                    throw new IOException(
                        String.format(
                            "failed to read %s from %s: %s",
                            this.key,
                            client.bucket(),
                            ex
                        ),
                        ex
                    );
                }
            }
            this.touched.set(System.currentTimeMillis());
            return this.data;
        }
    }

    /**
     * Is it expired?
     * @return TRUE if it is not required in memory any more
     * @throws IOException If fails
     */
    public boolean expired() throws IOException {
        final long mins = (System.currentTimeMillis() - this.touched.get())
            / TimeUnit.MINUTES.toMillis(1);
        return !this.dirty.get() && (mins > Tv.TWENTY || !this.valuable());
    }

    /**
     * Is it a valuable log?
     * @return TRUE if it is valuable and should be persisted
     * @throws IOException If fails
     */
    private boolean valuable() throws IOException {
        final Protocol protocol = new Protocol(
            new Protocol.Source() {
                @Override
                public InputStream stream() throws IOException {
                    return new ByteArrayInputStream(
                        Cache.this.data.toByteArray()
                    );
                }
            }
        );
        return !protocol.find("stage", "").isEmpty();
    }

}
