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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.VerboseThreads;
import com.rexsl.test.RestTester;
import com.rexsl.test.TestClient;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Stand;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.json.Json;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.xembly.XemblySyntaxException;

/**
 * Mirrored to a web {@link Stand}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @todo #162:0.5hr As soon as rexsl #716 is resolved remove
 *  SQSEntry interface and use TestClient directly.
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "origin", "work", "stand", "key" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.ExcessiveImports")
public final class Standed implements Drain {

    /**
     * Number of threads to use for sending to queue.
     */
    public static final int THREADS = 10;

    /**
     * Max message batch size.
     */
    private static final int MAX_BATCH_SIZE = 10;

    /**
     * SQS client connection container.
     */
    @Immutable
    private interface SQSEntry {

        /**
         * Provide SQS connection.
         * @return SQS connection.
         */
        TestClient get();
    }

    /**
     * Executor container.
     */
    @Immutable
    private interface Exec {

        /**
         * Provide executor.
         * @return ExecutorService
         */
        ExecutorService get();
    }

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
     * HTTP queue that will receive data.
     */
    private final transient Standed.SQSEntry entry;

    /**
     * Executor that run tasks.
     */
    private final transient Standed.Exec exec;

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
        this(
            wrk, name, secret, drain, RestTester.start(Stand.QUEUE),
            Executors.newFixedThreadPool(Standed.THREADS, new VerboseThreads())
        );
    }

    /**
     * Constructor for tests.
     * @param wrk Work we're in
     * @param name Name of stand
     * @param secret Secret key of the stand
     * @param drain Main drain
     * @param client HTTP queue
     * @param executor Executor to use
     * @checkstyle ParameterNumber (8 lines)
     */
    public Standed(final Work wrk, final String name, final String secret,
        final Drain drain, final TestClient client,
        final ExecutorService executor) {
        this.work = wrk;
        this.stand = name;
        this.key = secret;
        this.origin = drain;
        this.entry = new Standed.SQSEntry() {
            @Override
            public TestClient get() {
                return client;
            }
        };
        this.exec = new Standed.Exec() {
            @Override
            public ExecutorService get() {
                return executor;
            }
        };
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
    public Pageable<Time, Time> pulses() throws IOException {
        return this.origin.pulses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        final Iterable<List<String>> batches =
            Iterables.partition(
                FluentIterable.from(lines)
                    .filter(
                        new Predicate<String>() {
                            @Override
                            public boolean apply(final String line) {
                                return XemblyLine.existsIn(line);
                            }
                        }
                    )
                    .transform(
                        new Function<String, String>() {
                            @Override
                            public String apply(final String line) {
                                try {
                                    return XemblyLine.parse(line).xembly();
                                } catch (XemblySyntaxException ex) {
                                    Exceptions.warn(this, ex);
                                }
                                return null;
                            }
                        }
                    )
                    .filter(Predicates.notNull()), Standed.MAX_BATCH_SIZE
            );
        for (List<String> batchLines : batches) {
            this.send(batchLines);
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
                    "Standed: stand='%s'\n",
                    this.stand
                ),
                CharEncoding.UTF_8
            ),
            this.origin.read()
        );
    }

    /**
     * Send lines to stand.
     * @param xemblies The xembly scripts
     * @throws IOException If fails
     */
    private void send(final Iterable<String> xemblies) throws IOException {
        final List<String> messages = new ArrayList<String>();
        for (String xembly : xemblies) {
            messages.add(this.json(xembly));
        }
        final StringBuilder builder =
            new StringBuilder("Action=SendMessageBatch&Version=2011-10-01&");
        final List<String> postMessages = new ArrayList<String>();
        for (int identifier = 0; identifier < messages.size(); ++identifier) {
            postMessages.add(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "SendMessageBatchRequestEntry.%d.Id=%<d&SendMessageBatchRequestEntry.%<d.MessageBody=%s",
                    identifier + 1,
                    URLEncoder.encode(
                        messages.get(identifier), CharEncoding.UTF_8
                    )
                )
            );
        }
        builder.append(StringUtils.join(postMessages, '&'));
        final String body = builder.toString();
        final TestClient client = this.entry.get();
        this.exec.get().submit(
            new Callable<Void>() {
                @Override
                public Void call() throws IOException {
                    try {
                        final List<String> missed = Standed.this.enqueue(
                            client, body, messages.size()
                        );
                        if (!missed.isEmpty()) {
                            throw new IOException(
                                String.format(
                                    "Problem sending %d messages", missed.size()
                                )
                            );
                        }
                    } catch (AssertionError ex) {
                        throw new IOException(ex);
                    }
                    return null;
                }
            }
        );
    }

    /**
     * Put messages on SQS.
     * @param client HTTP client to use.
     * @param body Body of the POST.
     * @param size Number of messages sent.
     * @return List of message IDs that were not enqueued.
     * @throws UnsupportedEncodingException When unable to find UTF-8 encoding.
     */
    private List<String> enqueue(final TestClient client, final String body,
        final int size) throws UnsupportedEncodingException {
        return client.header(HttpHeaders.CONTENT_ENCODING, CharEncoding.UTF_8)
            .header(
                HttpHeaders.CONTENT_LENGTH,
                body.getBytes(CharEncoding.UTF_8).length
            )
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .post(
                String.format("sending %d lines to stand SQS queue", size),
                body
            )
            .assertStatus(HttpURLConnection.HTTP_OK)
                // @checkstyle LineLength (1 line)
            .xpath("/SendMessageBatchResponse/BatchResultError/BatchResultErrorEntry/Id/text()");
    }

    /**
     * Create a JSON representation of xembly.
     * @param xembly Xembly to change into json.
     * @return JSON of xembly.
     */
    private String json(final String xembly) {
        final StringWriter writer = new StringWriter();
        Json.createGenerator(writer)
            .writeStartObject()
            .write("nano", System.nanoTime())
            .write("stand", this.stand)
            .write("key", this.key)
            .write("xembly", xembly)
            .writeStartObject("work")
            .write("owner", this.work.owner().toString())
            .write("rule", this.work.rule())
            .write("scheduled", this.work.scheduled().toString())
            .writeEnd()
            .writeEnd()
            .close();
        return writer.toString();
    }
}
