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
package com.rultor.stateful.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.S3Client;
import com.rultor.stateful.Lineup;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Lineup with synchronization through S3 object/lock.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "client", "key" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class ObjectLineup implements Lineup {

    /**
     * Randomizer.
     */
    private static final Random RAND = new SecureRandom();

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Object name.
     */
    private final transient String key;

    /**
     * Public ctor.
     * @param obj Object name
     * @param clnt Client
     */
    public ObjectLineup(
        @NotNull(message = "object can't be NULL") final String obj,
        @NotNull(message = "S3 client can't be NULL") final S3Client clnt) {
        this.key = obj;
        this.client = clnt;
    }

    /**
     * Public ctor.
     * @param bkt Bucket name
     * @param obj Object key name
     * @param akey AWS key
     * @param scrt AWS secret
     * @checkstyle ParameterNumber (5 lines)
     */
    public ObjectLineup(final String bkt, final String obj, final String akey,
        final String scrt) {
        this(obj, new S3Client.Simple(akey, scrt, bkt));
    }

    @Override
    public <T> T exec(final Callable<T> callable) throws Exception {
        while (true) {
            while (this.exists()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(
                        ObjectLineup.RAND.nextInt(Tv.TEN * Tv.THOUSAND)
                    );
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
            }
            final String marker = String.format(
                "%d-%s",
                System.nanoTime(),
                RandomStringUtils.randomAlphanumeric(Tv.HUNDRED)
            );
            this.save(marker);
            final String saved = this.load();
            if (saved.equals(marker)) {
                Logger.debug(
                    this,
                    "S3 object `%s/%s` is locked by us with `%s`",
                    this.client.bucket(),
                    this.key,
                    saved
                );
                break;
            }
            Logger.debug(
                this,
                "S3 object `%s/%s` is locked by `%s`, let's wait...",
                this.client.bucket(),
                this.key,
                saved
            );
        }
        try {
            return callable.call();
        } finally {
            this.remove();
        }
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void exec(final Runnable runnable) {
        try {
            this.exec(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        runnable.run();
                        return null;
                    }
                }
            );
        // @checkstyle IllegalCatch (1 line)
        } catch (final Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Object exists in S3.
     * @return TRUE if it exists
     */
    private boolean exists() {
        final AmazonS3 aws = this.client.get();
        final ObjectListing listing =
            aws.listObjects(this.client.bucket(), this.key);
        final boolean exists = !listing.getObjectSummaries().isEmpty();
        if (exists) {
            Logger.debug(
                this,
                "S3 object `%s/%s` exists: %s",
                this.client.bucket(),
                this.key,
                this.load()
            );
        }
        return exists;
    }

    /**
     * Save text to S3 object.
     * @param content Content to save
     */
    private void save(final String content) {
        final AmazonS3 aws = this.client.get();
        try {
            final ObjectMetadata meta = new ObjectMetadata();
            meta.setContentEncoding(CharEncoding.UTF_8);
            meta.setContentLength(content.getBytes(CharEncoding.UTF_8).length);
            meta.setContentType(MediaType.TEXT_PLAIN);
            final PutObjectResult result = aws.putObject(
                this.client.bucket(), this.key,
                IOUtils.toInputStream(content, CharEncoding.UTF_8),
                meta
            );
            Logger.debug(
                this,
                "saved %d char(s) to S3 object `%s/%s`, etag=`%s`",
                content.length(),
                this.client.bucket(),
                this.key,
                result.getETag()
            );
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Load text from S3 object (or empty if it doesn't exist).
     * @return The content loaded
     */
    private String load() {
        final AmazonS3 aws = this.client.get();
        String content;
        try {
            final S3Object object =
                aws.getObject(this.client.bucket(), this.key);
            final InputStream stream = object.getObjectContent();
            try {
                content = IOUtils.toString(stream, CharEncoding.UTF_8);
                Logger.debug(
                    this,
                    "loaded %d char(s) from S3 object `%s/%s`",
                    content.length(),
                    this.client.bucket(),
                    this.key
                );
            } catch (final IOException ex) {
                throw new IllegalArgumentException(ex);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } catch (final AmazonS3Exception ex) {
            if (!ex.getMessage().contains("key does not exist")) {
                throw ex;
            }
            content = "";
        }
        return content;
    }

    /**
     * Remove object from S3.
     */
    private void remove() {
        final AmazonS3 aws = this.client.get();
        aws.deleteObject(this.client.bucket(), this.key);
        Logger.debug(
            this,
            "deleted S3 object `%s/%s`",
            this.client.bucket(),
            this.key
        );
    }

}
