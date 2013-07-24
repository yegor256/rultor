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
package com.rultor.board;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.S3Client;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Publish on of the arguments in S3 object.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "client", "prefix", "argument", "board" })
@Loggable(Loggable.DEBUG)
public final class S3Published implements Billboard {

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Prefix in the bucket.
     */
    private final transient String prefix;

    /**
     * Argument to publish.
     */
    private final transient String argument;

    /**
     * Argument to save URL to.
     */
    private final transient String destination;

    /**
     * Original board.
     */
    private final transient Billboard board;

    /**
     * Public ctor.
     * @param key S3 key
     * @param secret S3 secret
     * @param bucket S3 bucket name
     * @param pfx S3 bucket prefix
     * @param arg Argument to publish
     * @param dest Destination argument
     * @param brd Original board
     * @checkstyle ParameterNumber (10 lines)
     */
    public S3Published(
        @NotNull(message = "S3 key can't be NULL") final String key,
        @NotNull(message = "S3 secret can't be NULL") final String secret,
        @NotNull(message = "bucket name can't be NULL") final String bucket,
        @NotNull(message = "prefix can't be NULL") final String pfx,
        @NotNull(message = "argument can't be NULL") final String arg,
        @NotNull(message = "destination can't be NULL") final String dest,
        @NotNull(message = "board can't be NULL") final Billboard brd) {
        this.client = new S3Client.Simple(key, secret, bucket);
        this.prefix = pfx;
        this.argument = arg;
        this.destination = dest;
        this.board = brd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s with `%s` published in %s at `%s` and saved to `%s`",
            this.board, this.argument, this.client,
            this.prefix, this.destination
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(@NotNull(message = "announcement can't be NULL")
        final Announcement anmt) throws IOException {
        this.board.announce(
            new Announcement(
                anmt.level(),
                new ImmutableMap.Builder<String, Object>()
                    .putAll(anmt.args())
                    .put(
                        this.destination,
                        this.publish(anmt.args().get(this.argument).toString())
                    )
                    .build()
            )
        );
    }

    /**
     * Publish it to S3 and return its URL.
     * @param text The text to publish
     * @return URL
     * @throws IOException If something goes wrong
     */
    private URL publish(final String text) throws IOException {
        final AmazonS3 aws = this.client.get();
        final byte[] data = text.getBytes(CharEncoding.UTF_8);
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(data.length);
        meta.setContentType(MediaType.TEXT_HTML);
        final String key = String.format(
            "%s%tY/%<tm/%<td/%d.html", this.prefix, new Date(),
            System.nanoTime()
        );
        final PutObjectResult result = aws.putObject(
            this.client.bucket(), key,
            new ByteArrayInputStream(data),
            meta
        );
        Logger.info(
            this, "S3 object `%s` created in `%s`, etag=`%s`",
            key, this.client.bucket(),
            result.getETag()
        );
        return aws.generatePresignedUrl(
            this.client.bucket(), key,
            DateUtils.addDays(new Date(), Tv.TWENTY)
        );
    }

}
