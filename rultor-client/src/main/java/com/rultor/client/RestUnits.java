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
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * RESTful collection of units.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "home", "token" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class RestUnits implements Map<String, Unit> {

    /**
     * Home URI.
     */
    private final transient String home;

    /**
     * Authentication token.
     */
    private final transient String token;

    /**
     * Public ctor.
     * @param uri URI of home page
     * @param auth Authentication token
     */
    public RestUnits(final String uri, final String auth) {
        this.home = uri;
        this.token = auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return RestTester.start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(HttpHeaders.AUTHORIZATION, this.token)
            .get("#isEmpty()")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .nodes("/page/units/unit")
            .isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        return !RestTester.start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(HttpHeaders.AUTHORIZATION, this.token)
            .get(String.format("#containsKey(%s)", key))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .nodes(String.format("/page/units/unit[name='%s']", key))
            .isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit get(final Object key) {
        Unit unit = null;
        if (this.containsKey(key.toString())) {
            unit = new RestUnit(
                RestTester.start(UriBuilder.fromUri(this.home))
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                    .header(HttpHeaders.AUTHORIZATION, this.token)
                    .get(String.format("#get(%s)", key))
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .xpath(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "/page/units/unit[name='%s']/links/link[@rel='edit']/@href",
                            key
                        )
                    )
                    .get(0),
                this.token
            );
        }
        return unit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit put(final String key, final Unit value) {
        try {
            return new RestUnit(
                RestTester.start(UriBuilder.fromUri(this.home))
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                    .header(HttpHeaders.AUTHORIZATION, this.token)
                    .get(String.format("preparing to #create(%s)", key))
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .rel("/page/links/link[@rel='create']/@href")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                    .post(
                        String.format("#create(%s)", key),
                        String.format(
                            "name=%s",
                            URLEncoder.encode(key, CharEncoding.UTF_8)
                        )
                    )
                    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
                    .follow()
                    .uri()
                    .toString(),
                this.token
            );
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit remove(final Object key) {
        RestTester.start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(HttpHeaders.AUTHORIZATION, this.token)
            .get(String.format("preparing to #remove(%s)", key))
            .assertStatus(HttpURLConnection.HTTP_OK)
            .rel(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "/page/units/unit[name='%s']/links/link[@rel='remove']/@href",
                    key
                )
            )
            .get(String.format("#remove(%s)", key))
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        return new Unit() {
            @Override
            public void update(final Spec spec, final Spec drain) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Spec spec() {
                throw new UnsupportedOperationException();
            }
            @Override
            public Spec drain() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(final Map<? extends String, ? extends Unit> map) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Unit> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Unit>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
