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
import com.google.common.io.CountingInputStream;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.spi.Conveyer;
import com.rultor.spi.Pulse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
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
     * TRUE if this content is NOT synchronized with S3 and need to be flushed.
     */
    private final transient AtomicBoolean dirty = new AtomicBoolean();

    /**
     * New lines.
     */
    private final transient Collection<String> lines =
        new CopyOnWriteArrayList<String>();

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
        this.touched.set(System.currentTimeMillis());
        this.dirty.set(true);
        this.lines.add(line.toString());
    }

    /**
     * Read the entire object.
     * @return Input stream
     * @throws IOException If fails
     */
    public InputStream read() throws IOException {
        return this.stream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        synchronized (this.dirty) {
            if (this.dirty.get() && this.valuable()) {
                final S3Client client = this.key.client();
                final AmazonS3 aws = client.get();
                final CountingInputStream stream =
                    new CountingInputStream(this.stream());
                final ObjectMetadata meta = new ObjectMetadata();
                meta.setContentEncoding(CharEncoding.UTF_8);
                meta.setContentType(MediaType.TEXT_PLAIN);
                meta.setContentLength(this.data.size());
                try {
                    final PutObjectResult result = aws.putObject(
                        client.bucket(),
                        this.key.toString(),
                        stream,
                        meta
                    );
                    Logger.info(
                        this,
                        "'%s' saved %d byte(s) to S3, etag=%s",
                        this.key,
                        stream.getCount(),
                        result.getETag()
                    );
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
     * Age of the it in milliseconds.
     * @return Millis
     * @throws IOException If fails
     */
    public long age() throws IOException {
        return System.currentTimeMillis() - this.touched.get();
    }

    /**
     * Is it a valuable log?
     * @return TRUE if it is valuable and should be persisted
     * @throws IOException If fails
     */
    public boolean valuable() throws IOException {
        final Protocol protocol = new Protocol(
            new Protocol.Source() {
                @Override
                public InputStream stream() throws IOException {
                    return Cache.this.stream();
                }
            }
        );
        return !protocol.find(Pulse.Signal.STAGE, "").isEmpty();
    }

    /**
     * Get stream.
     * @return Stream with data
     * @throws IOException If fails
     */
    private InputStream stream() throws IOException {
        synchronized (this.dirty) {
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
            if (!this.lines.isEmpty()) {
                final PrintWriter writer = new PrintWriter(this.data);
                for (String line : this.lines) {
                    writer.append(line).append(CharUtils.LF);
                }
                writer.flush();
                writer.close();
                this.lines.clear();
            }
            return new ByteArrayInputStream(this.data.toByteArray());
        }
    }

}
