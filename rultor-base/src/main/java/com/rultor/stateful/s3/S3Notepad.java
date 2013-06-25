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
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.aws.S3Client;
import com.rultor.stateful.Notepad;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Collection in plain text S3 object (NOT thread-safe).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "client", "key" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class S3Notepad implements Notepad {

    /**
     * End of line marker.
     */
    private static final String EOL = "\n";

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
     * @param obj Object key name
     * @param clnt Client
     */
    public S3Notepad(@NotNull final String obj, @NotNull final S3Client clnt) {
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
    public S3Notepad(@NotNull final String obj, @NotNull final String bkt,
        @NotNull final String akey, @NotNull final String scrt) {
        this(obj, new S3Client.Simple(akey, scrt, bkt));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "S3 collection at %s/%s accessed with %s",
            this.client.bucket(), this.key, this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.load().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.load().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object object) {
        return this.load().contains(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        return this.load().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return this.load().toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(final T[] array) {
        return this.load().toArray(array);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final String line) {
        final Collection<String> remote = this.load();
        final boolean result = remote.add(line);
        this.save(remote);
        return result;
    }

    @Override
    public boolean remove(final Object line) {
        final Collection<String> remote = this.load();
        final boolean result = remote.remove(line);
        this.save(remote);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(final Collection<?> list) {
        return this.load().containsAll(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends String> list) {
        final Collection<String> remote = this.load();
        final boolean result = remote.addAll(list);
        this.save(remote);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> list) {
        final Collection<String> remote = this.load();
        final boolean result = remote.removeAll(list);
        this.save(remote);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> list) {
        final Collection<String> remote = this.load();
        final boolean result = remote.retainAll(list);
        this.save(remote);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.save(new ArrayList<String>(0));
    }

    /**
     * Load from S3.
     * @return List of strings just loaded
     */
    private Collection<String> load() {
        final AmazonS3 aws = this.client.get();
        final ObjectListing listing =
            aws.listObjects(this.client.bucket(), this.key);
        final Collection<String> list = new LinkedList<String>();
        if (listing.getObjectSummaries().isEmpty()) {
            Logger.info(
                this,
                "object '%s/%s' is absent in S3",
                this.client.bucket(),
                this.key
            );
        } else {
            final S3Object object =
                aws.getObject(this.client.bucket(), this.key);
            final String content;
            final InputStream stream = object.getObjectContent();
            try {
                content = IOUtils.toString(stream, CharEncoding.UTF_8);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            } finally {
                IOUtils.closeQuietly(stream);
            }
            Logger.info(
                this,
                "loaded %d char(s) from S3 object '%s/%s'",
                content.length(),
                this.client.bucket(),
                this.key
            );
            for (String line : content.split(S3Notepad.EOL)) {
                if (!line.isEmpty()) {
                    list.add(StringEscapeUtils.unescapeJava(line));
                }
            }
        }
        return list;
    }

    /**
     * Save to S3.
     * @param list List to save
     */
    private void save(final Collection<String> list) {
        final Collection<String> escaped = new ArrayList<String>(list.size());
        for (String line : list) {
            if (!line.isEmpty()) {
                escaped.add(StringEscapeUtils.escapeJava(line));
            }
        }
        final String content = StringUtils.join(escaped, S3Notepad.EOL);
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
            Logger.info(
                this,
                "saved %d char(s) to S3 object '%s/%s', etag=%s",
                content.length(),
                this.client.bucket(),
                this.key,
                result.getETag()
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
