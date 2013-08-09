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
package com.rultor.drain;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.log.Logger;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(of = { "lifetime", "work", "origin" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
public final class BufferedWrite implements Drain {

    /**
     * All in-memory buffers.
     * @checkstyle LineLength (2 lines)
     */
    private static final ConcurrentMap<BufferedWrite, BufferedWrite.Tunnel> TUNNELS =
        new ConcurrentHashMap<BufferedWrite, BufferedWrite.Tunnel>(0);

    /**
     * Flusher.
     */
    private static final Runnable FLUSH = new BufferedWrite.Flush();

    /**
     * How long to keep them in buffer, in milliseconds.
     */
    private final transient Long lifetime;

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Original drain.
     */
    private final transient Drain origin;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param sec How often should be flush, in seconds
     * @param drain Original drain
     */
    public BufferedWrite(
        @NotNull(message = "work can't be NULL") final Work wrk, final long sec,
        @NotNull(message = "drain can't be NULL") final Drain drain) {
        assert BufferedWrite.FLUSH != null;
        Validate.isTrue(
            sec <= TimeUnit.HOURS.toSeconds(1),
            "maximum interval allowed is one hour, while %d second(s) provided",
            sec
        );
        Validate.isTrue(
            sec >= 2,
            "minimum interval allowed is 2 seconds, while %d provided",
            sec
        );
        this.work = wrk;
        this.lifetime = TimeUnit.SECONDS.toMillis(sec);
        this.origin = drain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s with buffered write for %[ms]s",
            this.origin,
            this.lifetime
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return this.origin.pulses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        final BufferedWrite.Tunnel tunnel;
        synchronized (BufferedWrite.TUNNELS) {
            if (!BufferedWrite.TUNNELS.containsKey(this)) {
                BufferedWrite.TUNNELS.put(
                    this,
                    new BufferedWrite.Tunnel()
                );
            }
            tunnel = BufferedWrite.TUNNELS.get(this);
        }
        tunnel.send(lines);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                Logger.format(
                    "BufferedWrite: lifetime=%[ms]s, work='%s', origin='%s'\n",
                    this.lifetime,
                    this.work,
                    this.origin
                )
            ),
            this.origin.read()
        );
    }

    /**
     * Thread-safe tunnel to the real drain.
     */
    private final class Tunnel {
        /**
         * When was is started.
         */
        private final transient long start = System.currentTimeMillis();
        /**
         * Buffered data.
         */
        private final transient Collection<String> data =
            new CopyOnWriteArrayList<String>();
        /**
         * Send lines through.
         * @param lines Lines to send
         */
        public void send(final Iterable<String> lines) {
            Iterables.addAll(this.data, lines);
        }
        /**
         * Flush if necessary.
         * @return TRUE if it was flushed and should be removed
         * @throws IOException If fails
         */
        public boolean flush() throws IOException {
            final boolean expired = System.currentTimeMillis() - this.start
                > BufferedWrite.this.lifetime;
            if (expired && !this.data.isEmpty()) {
                BufferedWrite.this.origin.append(this.data);
                this.data.clear();
            }
            return expired;
        }
    }

    /**
     * Flush.
     */
    @ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.SECONDS)
    private static final class Flush implements Runnable {
        @Override
        public void run() {
            for (BufferedWrite client : BufferedWrite.TUNNELS.keySet()) {
                try {
                    if (BufferedWrite.TUNNELS.get(client).flush()) {
                        BufferedWrite.TUNNELS.remove(client);
                    }
                } catch (IOException ex) {
                    Logger.warn(this, "#run(): %s", ex);
                    BufferedWrite.TUNNELS.remove(client);
                }
            }
        }
    }

}
