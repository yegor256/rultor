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
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.spi.Metricable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Buffers.
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
final class Buffers implements Flushable, Metricable, Runnable {

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
     * Singleton instance.
     */
    public static final Buffers INSTANCE = new Buffers();

    /**
     * Semaphore to enable all-in-one-step flushing.
     */
    private final transient Semaphore semaphore =
        new Semaphore(Buffers.PERMITS, true);

    /**
     * All registered buffers.
     */
    private final transient ConcurrentMap<String, Buffer> all =
        new ConcurrentHashMap<String, Buffer>(0);

    /**
     * Private ctor.
     */
    private Buffers() {
        // intentionally empty
    }

    /**
     * Get a buffer.
     * @param owner Owner of it
     * @param unit Unit name
     * @param date When pulse started
     * @return The buffer
     */
    public Buffer get(final URN owner, final String unit, final long date) {
        final String key = String.format("%s %s %s", owner, unit, date);
        synchronized (this.all) {
            Buffer buffer = this.all.get(key);
            if (buffer == null) {
                buffer = new Buffer();
                this.all.put(key, buffer);
            }
            return buffer;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final MetricRegistry registry) {
        registry.register(
            MetricRegistry.name(this.getClass(), "buffers-total"),
            new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return Buffers.this.all.size();
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        for (Buffer buffer : this.all.values()) {
            buffer.flush();
        }
        try {
            for (Map.Entry<String, Buffer> entry : this.all.entrySet()) {
                if (this.expired(entry.getValue())) {
                    this.all.remove(entry.getKey());
                }
            }
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
     * This buffer is expired and should be removed?
     * @param buffer The buffer
     * @return TRUE if expired
     * @throws IOException If IO exception happens
     */
    private boolean expired(final Buffer buffer) throws IOException {
        final long mins = buffer.age() / TimeUnit.MINUTES.toMillis(1);
        return mins > Tv.THIRTY || (mins > Tv.FIVE && !buffer.valuable());
    }

}
