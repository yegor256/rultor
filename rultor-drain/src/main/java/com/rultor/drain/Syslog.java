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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.tools.Time;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.Charsets;

/**
 * Syslog.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @see <a href="http://tools.ietf.org/html/rfc5424">RFC 5424</a>
 */
@Immutable
@EqualsAndHashCode(of = { "host", "port", "priority" })
@Loggable(Loggable.DEBUG)
public final class Syslog implements Drain {

    /**
     * Host name.
     */
    private final transient String host;

    /**
     * UDP port number.
     */
    private final transient int port;

    /**
     * Facility.
     */
    private final transient int priority;

    /**
     * Public ctor.
     * @param hst Host
     * @param prt Port
     * @param pri Priority
     */
    public Syslog(
        @NotNull(message = "host can't be NULL") final String hst,
        final int prt, final int pri) {
        this.host = hst;
        this.port = prt;
        this.priority = pri;
    }

    /**
     * Public ctor.
     * @param hst Host
     * @param prt Port
     */
    public Syslog(final String hst, final int prt) {
        // @checkstyle MagicNumber (1 line)
        this(hst, prt, 14);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "`syslog://%s:%s`",
            this.host, this.port
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return new Pageable.Array<Time>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines)
        throws IOException {
        for (String line : lines) {
            this.send(this.compose(line));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        throw new IllegalArgumentException("there are no pulses");
    }

    /**
     * Send one packet.
     * @param bytes Bytes to send
     * @throws IOException If some IO problem
     */
    private void send(final byte[] bytes) throws IOException {
        final DatagramSocket socket = new DatagramSocket();
        socket.send(
            new DatagramPacket(
                bytes,
                bytes.length,
                InetAddress.getByName(this.host),
                this.port
            )
        );
    }

    /**
     * Compose a packet for Syslog.
     * @param text Text to use
     * @return Bytes for syslog
     * @see <a href="http://tools.ietf.org/html/rfc5424">RFC 5424</a>
     */
    private byte[] compose(final String text) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(baos, Charsets.UTF_8)
        );
        writer.append(String.format("<%d>1 - - - - - - ", this.priority));
        writer.flush();
        // @checkstyle MagicNumber (3 lines)
        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);
        writer.append(text);
        writer.flush();
        return baos.toByteArray();
    }

}
