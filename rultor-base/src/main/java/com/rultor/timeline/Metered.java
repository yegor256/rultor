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
package com.rultor.timeline;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rexsl.test.RestTester;
import com.rultor.spi.Instance;
import com.rultor.spi.Work;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.CharEncoding;
import org.apache.http.HttpHeaders;

/**
 * Metered instance.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "work", "origin", "timeline", "key" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class Metered implements Instance {

    /**
     * URL of the SQS queue.
     */
    private static final URI QUEUE = URI.create(
        "https://sqs.us-east-1.amazonaws.com/019644334823/rultor-meter"
    );

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Origin.
     */
    private final transient Instance origin;

    /**
     * Timeline name.
     */
    private final transient String timeline;

    /**
     * Timeline authentication key.
     */
    private final transient String key;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param name Name of timeline
     * @param auth Authentication key
     * @param inst Original instance
     * @checkstyle ParameterNumber (10 lines)
     */
    public Metered(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "name can't be NULL") final String name,
        @NotNull(message = "key can't be NULL") final String auth,
        @NotNull(message = "instance can't be NULL") final Instance inst) {
        this.work = wrk;
        this.timeline = name;
        this.key = auth;
        this.origin = inst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pulse() throws Exception {
        final Thread monitor = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                    try {
                        Metered.this.send("alive");
                    } catch (IOException ex) {
                        Logger.warn(this, "failed to meter: %s", ex);
                    }
                }
            }
        );
        this.send("start");
        this.origin.pulse();
        monitor.interrupt();
        this.send("stop");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s metered at `%s`",
            this.origin, this.timeline
        );
    }

    /**
     * Send JSON message to the queue.
     * @param msg Message to send
     * @throws IOException If IO problem inside
     */
    private void send(final String msg) throws IOException {
        final StringWriter writer = new StringWriter();
        Json.createGenerator(writer)
            .writeStartObject()
            .write("timeline", this.timeline)
            .write("key", this.key)
            .write("message", msg)
            .write("stdout", this.work.stdout().toString())
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
                .start(Metered.QUEUE)
                .header(HttpHeaders.CONTENT_ENCODING, CharEncoding.UTF_8)
                .header(
                    HttpHeaders.CONTENT_LENGTH,
                    body.getBytes(CharEncoding.UTF_8).length
                )
                .header(
                    HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_FORM_URLENCODED
                )
                .post("sending meter note", body)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } catch (AssertionError ex) {
            throw new IOException(ex);
        }
    }

}
