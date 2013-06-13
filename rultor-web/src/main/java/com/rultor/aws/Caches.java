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

import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import java.io.Flushable;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * In-memory cache of S3 objects.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode(of = { "client", "bucket" })
@Loggable(Loggable.DEBUG)
final class Caches implements Flushable {

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Bucket name.
     */
    private final transient String bucket;

    /**
     * All objects.
     */
    private final transient ConcurrentMap<Key, Cache> all =
        new ConcurrentSkipListMap<Key, Cache>();

    /**
     * Public ctor.
     * @param clnt Client
     * @param bkt Bucket name
     */
    protected Caches(final S3Client clnt, final String bkt) {
        this.client = clnt;
        this.bucket = bkt;
    }

    /**
     * Get cache by key.
     * @param key S3 key
     * @return Cache
     */
    public Cache get(final Key key) {
        this.all.putIfAbsent(key, new Cache(this.client, this.bucket, key));
        return this.all.get(key);
    }

    /**
     * Get a list of all keys available at the moment.
     * @param owner Owner
     * @param unit Unit
     * @return All keys
     */
    public SortedSet<Key> keys(final URN owner, final String unit) {
        final SortedSet<Key> keys = new TreeSet<Key>();
        for (Key key : this.all.keySet()) {
            if (key.belongsTo(owner, unit)) {
                keys.add(key);
            }
        }
        return keys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        for (Cache cache : this.all.values()) {
            cache.flush();
        }
    }

}
