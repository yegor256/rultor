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
import com.jcabi.urn.URN;
import com.rexsl.test.RestTester;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.CharEncoding;

/**
 * RESTful User.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "home", "token" })
@Loggable(Loggable.DEBUG)
public final class RestUser implements User {

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
     * @param urn User unique name in the system
     * @param key Secret authentication key
     */
    public RestUser(@NotNull final URI entry,
        @NotNull final URN urn, @NotNull final String key) {
        this.home = entry.toString();
        try {
            this.token = String.format(
                "Basic %s",
                Base64.encodeBase64String(
                    String.format(
                        "%s:%s",
                        URLEncoder.encode(urn.toString(), CharEncoding.UTF_8),
                        URLEncoder.encode(key, CharEncoding.UTF_8)
                    ).getBytes(Charsets.UTF_8)
                )
            );
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Public ctor.
     * @param urn User unique name in the system
     * @param key Secret authentication key
     */
    public RestUser(@NotNull final URN urn, @NotNull final String key) {
        this(URI.create("http://www.rultor.com"), urn, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN urn() {
        return URN.create(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, this.token)
                .get("#urn()")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertXPath("/page/identity")
                .xpath("/page/identity/urn/text()")
                .get(0)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> units() {
        return new HashSet<String>(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, this.token)
                .get("#isEmpty()")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .xpath("/page/units/unit/name/text()")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit get(final String name) {
        return new RestUnit(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, this.token)
                .get(String.format("#get(%s)", name))
                .assertStatus(HttpURLConnection.HTTP_OK)
                .xpath(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "/page/units/unit[name='%s']/links/link[@rel='edit']/@href",
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
                    "/page/units/unit[name='%s']/links/link[@rel='remove']/@href",
                    name
                )
            )
            .get(String.format("#remove(%s)", name))
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

}
