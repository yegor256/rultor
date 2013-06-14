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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Flushables;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.urn.URN;
import com.rultor.spi.Conveyer;
import com.rultor.spi.Metricable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Mutable and thread-safe in-memory cache of S3 objects (singleton).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
@ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.MINUTES)
@SuppressWarnings("PMD.DoNotUseThreads")
final class Caches implements Flushable, Metricable, Runnable {

    /**
     * Total number of available parallel permits.
     *
     * <p>This is how many parallel threads can get access to this class. The
     * number is used in order to enable flushing. When flushing is happening
     * we should disable all IO operations with the class. To do so,
     * {@link Semaphore} is used, and it requires a limited number of
     * permits on construction.
     */
    public static final int PERMITS = 10;

    /**
     * Instance of the singleton.
     */
    public static final Caches INSTANCE = new Caches();

    /**
     * All objects.
     */
    private final transient ConcurrentMap<Key, Cache> all =
        new ConcurrentSkipListMap<Key, Cache>();

    /**
     * Semaphore to enable all-in-one-step flushing.
     */
    private final transient Semaphore semaphore =
        new Semaphore(Caches.PERMITS, true);

    /**
     * Private ctor.
     */
    private Caches() {
        // it's a singleton
    }

    /**
     * Append new line to an object.
     * @param key Key to append to
     * @param line Line to add
     * @throws IOException If fails
     */
    public void append(final Key key,
        final Conveyer.Line line) throws IOException {
        try {
            try {
                this.semaphore.acquire();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
            this.get(key).append(line);
        } finally {
            this.semaphore.release();
        }
    }

    /**
     * Read the entire object.
     * @param key Which stream to read
     * @return Input stream
     * @throws IOException If fails
     */
    public InputStream read(final Key key) throws IOException {
        try {
            try {
                this.semaphore.acquire();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
            return this.get(key).read();
        } finally {
            this.semaphore.release();
        }
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
    public void register(final MetricRegistry registry) {
        registry.register(
            MetricRegistry.name(this.getClass(), "keys-total"),
            new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return Caches.this.all.size();
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        for (Cache cache : this.all.values()) {
            cache.flush();
        }
        try {
            try {
                this.semaphore.acquire(Caches.PERMITS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
            this.clean();
        } finally {
            this.semaphore.release(Caches.PERMITS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        Flushables.flushQuietly(this);
    }

    /**
     * Get cache by key.
     * @param key S3 key
     * @return Cache
     */
    private Cache get(final Key key) {
        this.all.putIfAbsent(key, new Cache(key));
        return this.all.get(key);
    }

    /**
     * Remove expired elements.
     */
    @Loggable(Loggable.INFO)
    private void clean() {
        for (Map.Entry<Key, Cache> entry : this.all.entrySet()) {
            if (entry.getValue().expired()) {
                this.all.remove(entry.getKey());
            }
        }
    }

}
