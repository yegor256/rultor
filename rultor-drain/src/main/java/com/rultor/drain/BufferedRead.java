/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.drain;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.tools.Time;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * Buffered in memory.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "work", "lifetime", "origin" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
public final class BufferedRead implements Drain, Closeable {

    /**
     * All in-memory buffers.
     * @checkstyle LineLength (2 lines)
     */
    private static final ConcurrentMap<BufferedRead, BufferedRead.Buffer> BUFFERS =
        new ConcurrentHashMap<BufferedRead, BufferedRead.Buffer>(0);

    /**
     * Cleaner of memory.
     */
    private static final Runnable CLEANER = new BufferedRead.Cleaner();

    /**
     * How long to keep them in buffer, in milliseconds.
     */
    private final transient Long lifetime;

    /**
     * Coordinates we're in.
     */
    private final transient Coordinates work;

    /**
     * Original drain.
     */
    private final transient Drain origin;

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param sec For how long to keep them in memory
     * @param drain Original drain
     */
    public BufferedRead(
        @NotNull(message = "work can't be NULL") final Coordinates wrk,
        final long sec,
        @NotNull(message = "drain can't be NULL") final Drain drain) {
        assert BufferedRead.CLEANER != null;
        Validate.isTrue(
            sec <= TimeUnit.HOURS.toSeconds(1L),
            "maximum interval allowed is one hour, while %d second(s) provided",
            sec
        );
        Validate.isTrue(
            sec >= 2L,
            "minimum interval allowed is 2 seconds, while %d provided",
            sec
        );
        this.work = wrk;
        this.lifetime = TimeUnit.SECONDS.toMillis(sec);
        this.origin = drain;
    }

    @Override
    public void close() {
        BufferedRead.CLEANER.run();
    }

    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return this.origin.pulses();
    }

    @Override
    public void append(final Iterable<String> lines) throws IOException {
        this.origin.append(lines);
        BufferedRead.BUFFERS.remove(this);
    }

    @Override
    public InputStream read() throws IOException {
        final BufferedRead.Buffer buffer;
        synchronized (BufferedRead.BUFFERS) {
            if (!BufferedRead.BUFFERS.containsKey(this)) {
                BufferedRead.BUFFERS.put(
                    this, new BufferedRead.Buffer()
                );
            }
            buffer = BufferedRead.BUFFERS.get(this);
        }
        assert buffer != null : "buffer can't be NULL due to synchronization";
        return new SequenceInputStream(
            IOUtils.toInputStream(
                Logger.format(
                    "BufferedRead: lifetime=%[ms]s, work='%s'\n",
                    this.lifetime, this.work
                )
            ),
            buffer.stream()
        );
    }

    /**
     * Thread-safe buffer to the real drain.
     */
    private final class Buffer {
        /**
         * When was is scheduled.
         */
        private final transient long start = System.currentTimeMillis();
        /**
         * Buffered data.
         */
        private final transient byte[] data;
        /**
         * Public ctor.
         * @throws IOException If some error with the stream
         */
        Buffer() throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copyLarge(BufferedRead.this.origin.read(), baos);
            this.data = baos.toByteArray();
        }
        /**
         * Read it as a stream.
         * @return Stream
         */
        public InputStream stream() {
            return new ByteArrayInputStream(this.data);
        }
        /**
         * Is it too old?
         * @return TRUE if it is too old
         */
        public boolean expired() {
            return System.currentTimeMillis() - this.start
                > BufferedRead.this.lifetime;
        }
    }

    /**
     * Flush.
     */
    @ToString
    @Immutable
    @EqualsAndHashCode
    @ScheduleWithFixedDelay(
        delay = 1, unit = TimeUnit.SECONDS,
        await = 1, awaitUnit = TimeUnit.MINUTES,
        shutdownAttempts = Tv.FIVE
    )
    private static final class Cleaner implements Runnable {
        @Override
        public void run() {
            for (final BufferedRead client : BufferedRead.BUFFERS.keySet()) {
                if (BufferedRead.BUFFERS.get(client).expired()) {
                    BufferedRead.BUFFERS.remove(client);
                }
            }
        }
    }

}
