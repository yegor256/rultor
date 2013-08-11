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
package com.rultor.conveyer;

import com.google.common.collect.ImmutableBiMap;
import com.jcabi.aspects.Tv;
import com.rultor.conveyer.http.Streams;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Streams from Log4j.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@EqualsAndHashCode(callSuper = false, of = { "groups", "buffers" })
public final class Log4jStreams extends AppenderSkeleton implements Streams {

    /**
     * Thread groups to keys.
     */
    private final transient ConcurrentMap<ThreadGroup, String> groups =
        new ConcurrentHashMap<ThreadGroup, String>(0);

    /**
     * Keys to buffers.
     */
    private final transient ConcurrentMap<String, CircularBuffer> buffers =
        new ConcurrentHashMap<String, CircularBuffer>(0);

    /**
     * {@inheritDoc}
     */
    @Override
    public String register() {
        final String key = DigestUtils.md5Hex(
            RandomStringUtils.random(Tv.FIFTY)
        );
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        if (this.groups.put(group, key) != null) {
            throw new IllegalStateException("call unregister() first");
        }
        this.buffers.put(key, new CircularBuffer());
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister(final String key) {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        if (!this.groups.remove(group, key)) {
            throw new IllegalStateException("call register() first");
        }
        this.buffers.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream stream(final String key) {
        return new SequenceInputStream(
            IOUtils.toInputStream(String.format("Listening to %s...\n\n", key)),
            new InputStream() {
                @Override
                public int read() throws IOException {
                    return Log4jStreams.this.read();
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt(final String key) {
        ImmutableBiMap.copyOf(this.groups).inverse().get(key).interrupt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void append(final LoggingEvent event) {
        final String key = this.groups.get(
            Thread.currentThread().getThreadGroup()
        );
        if (key != null) {
            final CircularBuffer buffer = this.buffers.get(key);
            if (buffer != null) {
                final byte[] bytes;
                try {
                    bytes = event.getRenderedMessage()
                        .getBytes(CharEncoding.UTF_8);
                } catch (UnsupportedEncodingException ex) {
                    throw new IllegalStateException(ex);
                }
                for (byte data : bytes) {
                    buffer.write(data);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder()
            .append(this.getClass().getCanonicalName()).append('\n');
        for (Map.Entry<ThreadGroup, String> entry : this.groups.entrySet()) {
            text.append(entry.getKey())
                .append(": ")
                .append(entry.getValue().substring(0, Tv.FIVE))
                .append("...\n");
        }
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

    /**
     * Read next byte.
     * @return The byte
     */
    private byte read() {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        String key;
        while (true) {
            key = this.groups.get(group);
            if (key != null) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
        final CircularBuffer buffer = this.buffers.get(key);
        if (buffer == null) {
            throw new IllegalStateException("buffer is gone");
        }
        while (buffer.isEmpty()) {
            try {
                TimeUnit.MICROSECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
        return buffer.read();
    }

}
