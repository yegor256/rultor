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
package com.rultor.client;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rexsl.test.RestTester;
import com.rultor.spi.Rule;
import com.rultor.spi.Rules;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * RESTful Rules.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "home", "token" })
@Loggable(Loggable.DEBUG)
final class RestRules implements Rules {

    /**
     * Home URI.
     */
    private final transient String home;

    /**
     * Authentication token.
     */
    private final transient String token;

    /**
     * Public ctor, with custom entry point.
     * @param entry Entry point (URI)
     * @param tkn Token
     */
    protected RestRules(
        @NotNull(message = "URI can't be NULL") final URI entry,
        @NotNull(message = "token can't be NULL") final String tkn) {
        this.home = entry.toString();
        this.token = tkn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Rule> iterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rule get(final String name) {
        return new RestRule(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, this.token)
                .get(String.format("#get(%s)", name))
                .assertStatus(HttpURLConnection.HTTP_OK)
                .xpath(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "/page/rules/rule[name='%s']/links/link[@rel='edit']/@href",
                        name
                    )
                )
                .get(0),
            this.token
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final String name) {
        try {
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, this.token)
                .get(String.format("preparing to #create(%s)", name))
                .assertStatus(HttpURLConnection.HTTP_OK)
                .rel("/page/links/link[@rel='create']/@href")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .post(
                    String.format("#create(%s)", name),
                    String.format(
                        "name=%s",
                        URLEncoder.encode(name, CharEncoding.UTF_8)
                    )
                )
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String name) {
        RestTester.start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(HttpHeaders.AUTHORIZATION, this.token)
            .get(String.format("preparing to #remove(%s)", name))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .rel(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "/page/rules/rule[name='%s']/links/link[@rel='remove']/@href",
                    name
                )
            )
            .get(String.format("#remove(%s)", name))
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String name) {
        return !RestTester.start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(HttpHeaders.AUTHORIZATION, this.token)
            .get(String.format("#contains(%s)", name))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .xpath(String.format("/page/rules/rule[name='%s']", name))
            .isEmpty();
    }

}
