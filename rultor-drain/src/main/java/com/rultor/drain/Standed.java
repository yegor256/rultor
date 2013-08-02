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
import com.rexsl.test.RestTester;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Stand;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import javax.json.Json;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.HttpHeaders;

/**
 * Mirrored to a web {@link Stand}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class Standed implements Drain {

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Original drain.
     */
    private final transient Drain origin;

    /**
     * Name of stand.
     */
    private final transient String stand;

    /**
     * Secret key of it.
     */
    private final transient String key;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param name Name of stand
     * @param secret Secret key of the stand
     * @param drain Main drain
     * @checkstyle ParameterNumber (8 lines)
     */
    public Standed(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "name of stand can't be NULL") final String name,
        @NotNull(message = "key can't be NULL") final String secret,
        @NotNull(message = "drain can't be NULL") final Drain drain) {
        this.work = wrk;
        this.stand = name;
        this.key = secret;
        this.origin = drain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s standed at `%s`",
            this.origin, this.stand
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time> pulses() throws IOException {
        return this.origin.pulses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        for (String line : lines) {
            this.send(line);
        }
        this.origin.append(lines);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "Standed: stand='%s', origin='%s'\n",
                    this.stand, this.origin
                )
            ),
            this.origin.read()
        );
    }

    /**
     * Send line to stand.
     * @param line The line
     * @throws IOException If fails
     */
    private void send(final String line) throws IOException {
        final StringWriter writer = new StringWriter();
        Json.createGenerator(writer)
            .writeStartObject()
            .write("user", this.work.owner().toString())
            .write("stand", this.stand)
            .write("key", this.key)
            .write("xembly", line)
            .writeStartObject("work")
            .write("owner", this.work.owner().toString())
            .write("unit", this.work.unit())
            .write("started", this.work.started().toString())
            .writeEnd()
            .writeEnd()
            .close();
        final String body = String.format(
            "Action=SendMessage&Version=2011-10-01&MessageBody=%s",
            URLEncoder.encode(writer.toString(), CharEncoding.UTF_8)
        );
        try {
            RestTester
                .start(Stand.QUEUE)
                .header(HttpHeaders.CONTENT_ENCODING, CharEncoding.UTF_8)
                .header(
                    HttpHeaders.CONTENT_LENGTH,
                    body.getBytes(CharEncoding.UTF_8).length
                )
                .header(
                    HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_FORM_URLENCODED
                )
                .post("sending one line to stand SQS queue", body)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } catch (AssertionError ex) {
            throw new IOException(ex);
        }
    }

}
