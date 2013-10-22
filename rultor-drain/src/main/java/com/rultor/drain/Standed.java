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
import com.google.common.util.concurrent.MoreExecutors;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.rexsl.test.RestTester;
import com.rexsl.test.TestClient;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Stand;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import javax.json.Json;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.xembly.SyntaxException;

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
@ToString
@EqualsAndHashCode(of = { "origin", "work", "stand", "key" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.TooManyMethods" })
public final class Standed implements Drain {

    /**
     * Randomizer for nanos.
     */
    public static final Random RND = new SecureRandom();

    /**
     * Number of threads to use for sending to queue.
     */
    public static final int THREADS = 10;

    /**
     * Max message batch size.
     */
    private static final int MAX = 10;

    /**
     * Coordinates we're in.
     */
    private final transient Coordinates work;

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
     * Executor that runs tasks.
     */
    private final transient Standed.Exec exec;

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param name Name of stand
     * @param secret Secret key of the stand
     * @param drain Main drain
     * @checkstyle ParameterNumber (8 lines)
     * @todo #285 Due to a problem with concurrency we're using this
     *  same thread executor. When the problem is fixed we should use
     *  Executors.newFixedThreadPool(Standed.THREADS, new VerboseThreads())
     */
    public Standed(
        @NotNull(message = "work can't be NULL") final Coordinates wrk,
        @NotNull(message = "name of stand can't be NULL") final String name,
        @NotNull(message = "key can't be NULL") final String secret,
        @NotNull(message = "drain can't be NULL") final Drain drain) {
        this(
            wrk, name, secret, drain, RestTester.start(Stand.QUEUE),
            MoreExecutors.sameThreadExecutor()
        );
    }

    /**
     * Constructor for tests.
     * @param wrk Coordinates we're in
     * @param name Name of stand
     * @param secret Secret key of the stand
     * @param drain Main drain
     * @param client HTTP queue
     * @param executor Executor to use
     * @checkstyle ParameterNumber (8 lines)
     */
    public Standed(final Coordinates wrk, final String name,
        final String secret,
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

    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return this.origin.pulses();
    }

    @Override
    public void append(final Iterable<String> lines) throws IOException {
        final Iterable<List<String>> batches = Iterables.partition(
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
                            } catch (SyntaxException ex) {
                                Exceptions.warn(this, ex);
                            }
                            return null;
                        }
                    }
                )
                .filter(Predicates.notNull()), Standed.MAX
        );
        for (List<String> batch : batches) {
            this.send(batch);
        }
        this.origin.append(lines);
    }

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
        this.exec.get().submit(
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        Standed.this.send(Standed.this.body(xemblies));
                        return null;
                    }
                },
                true, false
            )
        );
    }

    /**
     * Send the message.
     * @param body POST request body
     * @throws IOException If fails
     */
    @RetryOnFailure(verbose = false, attempts = Tv.TEN)
    private void send(final String body) throws IOException {
        final Collection<String> missed = Standed.this.enqueue(
            this.entry.get(), body
        );
        if (!missed.isEmpty()) {
            throw new IOException(
                String.format(
                    "Problem with sending of %d message(s): %s",
                    missed.size(), StringUtils.join(missed, ", ")
                )
            );
        }
    }

    /**
     * Create POST request body.
     * @param xemblies The xembly scripts
     * @return POST request body
     * @throws IOException If fails
     */
    private String body(final Iterable<String> xemblies) throws IOException {
        final List<String> msgs = new ArrayList<String>(0);
        for (String xembly : xemblies) {
            msgs.add(this.json(xembly));
        }
        final StringBuilder body = new StringBuilder()
            .append("Action=SendMessageBatch")
            .append("&Version=2011-10-01");
        for (int idx = 0; idx < msgs.size(); ++idx) {
            final int ent = idx + 1;
            body.append('&')
                .append("SendMessageBatchRequestEntry.")
                .append(ent)
                .append(".Id=")
                .append(ent)
                .append("&SendMessageBatchRequestEntry.")
                .append(ent)
                .append(".MessageBody=")
                .append(URLEncoder.encode(msgs.get(idx), CharEncoding.UTF_8));
        }
        return body.toString();
    }

    /**
     * Put messages on SQS.
     * @param client HTTP client to use.
     * @param body Body of the POST.
     * @return List of message IDs that were not enqueued.
     * @throws UnsupportedEncodingException When unable to find UTF-8 encoding.
     */
    private List<String> enqueue(final TestClient client, final String body)
        throws UnsupportedEncodingException {
        return client.header(HttpHeaders.CONTENT_ENCODING, CharEncoding.UTF_8)
            .header(
                HttpHeaders.CONTENT_LENGTH,
                body.getBytes(CharEncoding.UTF_8).length
            )
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .post("sending batch of lines to stand SQS queue", body)
            .assertStatus(HttpURLConnection.HTTP_OK)
            // @checkstyle LineLength (1 line)
            .xpath("/SendMessageBatchResponse/BatchResultError/BatchResultErrorEntry/Id/text()");
    }

    /**
     * Create a JSON representation of xembly.
     * @param xembly Xembly to change into JSON.
     * @return JSON of xembly.
     */
    private String json(final String xembly) {
        final StringWriter writer = new StringWriter();
        final long nano = System.nanoTime() * Tv.HUNDRED
            + Standed.RND.nextInt(Tv.HUNDRED);
        Json.createGenerator(writer)
            .writeStartObject()
            .write("stand", this.stand)
            .write("nano", nano)
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

}
