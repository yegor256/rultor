/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.web;

import com.jcabi.log.Logger;
import com.rultor.agents.daemons.Tail;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.input.ProxyReader;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.text.StringEscapeUtils;
import org.takes.Response;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsFluent;

/**
 * Single daemon.
 *
 * @since 1.50
 */
final class TkDaemon implements TkRegex {

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Ctor.
     * @param tlks Talks
     */
    TkDaemon(final Talks tlks) {
        this.talks = tlks;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final long number = Long.parseLong(req.matcher().group(1));
        if (!this.talks.exists(number)) {
            throw new RsForward(
                new RsFlash(
                    "There is no such page here",
                    Level.WARNING
                )
            );
        }
        final RqUser user = new RqUser(req);
        if (!user.canSee(this.talks.get(number))) {
            throw new RsForward(
                new RsFlash(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "According to .rultor.yml, you (%s) are not allowed to see this",
                        user
                    ),
                    Level.WARNING
                )
            );
        }
        final String hash = req.matcher().group(2);
        try {
            return new RsFluent()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withBody(this.html(number, hash))
                .withType("text/html; charset=utf-8")
                .withHeader(
                    "X-Rultor-Daemon",
                    String.format("%s-%s", number, hash)
                );
        } catch (final IOException err) {
            Logger.error(
                this,
                "Error during answering in talk %d to %s. %s",
                hash, err
            );
            throw err;
        }
    }

    /**
     * Get HTML.
     * @param number Number
     * @param hash Hash
     * @return HTML
     * @throws IOException If fails
     */
    private InputStream html(final long number, final String hash)
        throws IOException {
        final Talk talk = this.talks.get(number);
        final String head = IOUtils.toString(
            this.getClass().getResource("daemon/head.html"),
            StandardCharsets.UTF_8
        ).trim();
        return new SequenceInputStream(
            Collections.enumeration(
                Arrays.asList(
                    IOUtils.toInputStream(
                        head.replace("TALK_NAME", talk.name())
                            .replace(
                                "TALK_LINK",
                                StringEscapeUtils.escapeHtml4(
                                    talk.read()
                                        .xpath("/talk/wire/href/text()").get(0)
                                )
                            ),
                        StandardCharsets.UTF_8
                    ),
                    TkDaemon.escape(new Tail(talk.read(), hash).read()),
                    AutoCloseInputStream.builder()
                        .setInputStream(
                            this.getClass().getResourceAsStream("daemon/tail.html")
                        ).get()
                )
            )
        );
    }

    /**
     * Escape HTML chars in input stream.
     * @param input Input stream
     * @return New input stream
     * @throws IOException If fails
     */
    private static InputStream escape(
        final InputStream input
    ) throws IOException {
        final PushbackReader src = new PushbackReader(
            new InputStreamReader(input, StandardCharsets.UTF_8),
            100_000
        );
        return ReaderInputStream.builder()
            .setCharset(StandardCharsets.UTF_8)
            .setReader(
                // @checkstyle AnonInnerLengthCheck (30 lines)
                new ProxyReader(src) {
                    @Override
                    protected void beforeRead(final int len)
                        throws IOException {
                        super.beforeRead(len);
                        final char[] buf = new char[len];
                        final int found = src.read(buf);
                        if (found > 0) {
                            final StringBuilder line =
                                new StringBuilder(found);
                            for (int idx = 0; idx < found; ++idx) {
                                line.append(buf[idx]);
                            }
                            final String escape =
                                StringEscapeUtils.escapeHtml4(
                                    line.toString()
                                );
                            final char[] rpl = new char[escape.length()];
                            escape.getChars(0, escape.length(), rpl, 0);
                            src.unread(rpl);
                        }
                    }
                }
            ).get();
    }

}
