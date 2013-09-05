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

import com.google.common.base.Charsets;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.log.Logger;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Temporary in memory.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "work", "marker" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
public final class Temporary implements Drain {

    /**
     * Maximum lifetime allowed, in milliseconds.
     */
    private static final long LIFETIME = TimeUnit.HOURS.toMillis(2);

    /**
     * All in-memory buffers.
     */
    private static final ConcurrentMap<Temporary, Temporary.Buffer> BUFFERS =
        new ConcurrentHashMap<Temporary, Temporary.Buffer>(0);

    /**
     * Cleaner.
     */
    private static final Runnable CLEANER = new Temporary.Cleaner();

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Optional marker.
     */
    private final transient String marker;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param mrk Optional marker
     */
    public Temporary(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "marker can't be NULL") final String mrk) {
        assert Temporary.CLEANER != null;
        this.work = wrk;
        this.marker = mrk;
    }

    /**
     * Public ctor.
     * @param wrk Work we're in
     */
    public Temporary(final Work wrk) {
        this(wrk, "def");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("temporary marked as `%s`", this.marker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        final Collection<Time> times = new LinkedList<Time>();
        for (Temporary client : Temporary.BUFFERS.keySet()) {
            if (this.similar(client)) {
                times.add(client.work.scheduled());
            }
        }
        return new Pageable.Array<Time>(times);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        final Temporary.Buffer buffer;
        synchronized (Temporary.BUFFERS) {
            if (!Temporary.BUFFERS.containsKey(this)) {
                Temporary.BUFFERS.put(
                    this,
                    new Temporary.Buffer()
                );
            }
            buffer = Temporary.BUFFERS.get(this);
        }
        buffer.append(lines);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        final Temporary.Buffer buffer = Temporary.BUFFERS.get(this);
        if (buffer == null) {
            throw new IOException(
                String.format(
                    "temporary buffer is absent for `%s` in `%s`",
                    this.marker,
                    this.work
                )
            );
        }
        return new SequenceInputStream(
            IOUtils.toInputStream(
                Logger.format(
                    "Temporary: marker='%s', work='%s', buffer='%s'\n",
                    this.marker,
                    this.work,
                    buffer
                ),
                CharEncoding.UTF_8
            ),
            buffer.read()
        );
    }

    /**
     * Thread-safe buffer to the real drain.
     */
    private final class Buffer {
        /**
         * When was is scheduled.
         */
        private final transient Long start = System.currentTimeMillis();
        /**
         * Buffered data.
         */
        private final transient ByteArrayOutputStream data =
            new ByteArrayOutputStream();
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return Logger.format(
                "age=%[ms]s, size=%d",
                System.currentTimeMillis() - this.start,
                this.data.size()
            );
        }
        /**
         * Append lines to the buffer.
         * @param lines Lines to append
         */
        public void append(final Iterable<String> lines) {
            synchronized (this.start) {
                final PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(this.data, Charsets.UTF_8)
                );
                for (String line : lines) {
                    writer.print(line);
                    writer.print('\n');
                }
                writer.flush();
                writer.close();
            }
        }
        /**
         * Read it as a stream.
         * @return The stream
         */
        public InputStream read() {
            return new ByteArrayInputStream(this.data.toByteArray());
        }
        /**
         * Expired already?
         * @return TRUE if it is too old
         */
        public boolean expired() {
            return System.currentTimeMillis() - this.start
                > Temporary.LIFETIME;
        }
    }

    /**
     * This drain is similar to the one provided?
     * @param drain Drain to compare with
     * @return TRUE if they are similar
     */
    private boolean similar(final Temporary drain) {
        return this.work.owner().equals(drain.work.owner())
            && this.work.rule().equals(drain.work.rule())
            && this.marker.equals(drain.marker);
    }

    /**
     * Flush.
     */
    @ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.SECONDS)
    private static final class Cleaner implements Runnable {
        @Override
        public void run() {
            for (Temporary client : Temporary.BUFFERS.keySet()) {
                if (Temporary.BUFFERS.get(client).expired()) {
                    Temporary.BUFFERS.remove(client);
                }
            }
        }
    }

}
