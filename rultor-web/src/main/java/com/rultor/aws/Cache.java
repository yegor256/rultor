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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;

/**
 * In-memory cache of a single S3 object.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode(of = { "client", "bucket", "key" })
@Loggable(Loggable.DEBUG)
final class Cache implements Flushable {

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Bucket name.
     */
    private final transient String bucket;

    /**
     * S3 key.
     */
    private final transient String key;

    /**
     * Data.
     */
    private transient ByteArrayOutputStream data = null;

    /**
     * Public ctor.
     * @param clnt Client
     * @param bkt Bucket name
     * @param akey S3 key
     */
    protected Cache(final S3Client clnt, final String bkt, final String akey) {
        this.client = clnt;
        this.bucket = bkt;
        this.key = akey;
    }

    /**
     * Append new line to an object.
     * @param line Line to add
     * @throws IOException If fails
     */
    public void append(final String line) throws IOException {
        new PrintWriter(this.stream()).append(CharUtils.LF).append(line);
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
        synchronized (this.bucket) {
            if (this.data != null) {
                final AmazonS3 aws = this.client.get();
                final ObjectMetadata meta = new ObjectMetadata();
                meta.setContentEncoding(CharEncoding.UTF_8);
                meta.setContentLength(this.data.size());
                meta.setContentType(MediaType.TEXT_PLAIN);
                final PutObjectResult result = aws.putObject(
                    this.bucket, this.key, this.read(), meta
                );
                Logger.info(
                    this,
                    "'%s' saved to S3, size=%d, etag=%s",
                    this.key,
                    this.data.size(),
                    result.getETag()
                );
            }
        }
    }

    /**
     * Get stream.
     * @return Stream with data
     * @throws IOException If fails
     */
    private ByteArrayOutputStream stream() throws IOException {
        synchronized (this.bucket) {
            if (this.data == null) {
                this.data = new ByteArrayOutputStream();
                final AmazonS3 aws = this.client.get();
                final S3Object object = aws.getObject(this.bucket, this.key);
                IOUtils.copy(object.getObjectContent(), this.data);
                Logger.info(
                    this,
                    "'%s' loaded from S3, size=%d, etag=%s",
                    this.key,
                    this.data.size(),
                    object.getObjectMetadata().getETag()
                );
            }
            return this.data;
        }
    }

}
