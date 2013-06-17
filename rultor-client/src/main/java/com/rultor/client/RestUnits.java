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
import com.rultor.spi.Pulse;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * RESTful User.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "home", "cookie" })
@Loggable(Loggable.DEBUG)
public final class RestUnits implements Map<String, Unit> {

    /**
     * Home URI.
     */
    private final transient String home;

    /**
     * Authentication cookie.
     */
    private final transient String cookie;

    /**
     * Public ctor.
     * @param uri URI of home page
     * @param auth Authentication cookie
     */
    public RestUnits(final String uri, final String auth) {
        this.home = uri;
        this.cookie = auth;
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
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .get("home page with all units")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .xpath("/page/units/unit")
            .isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        boolean contains = false;
        if (key instanceof String) {
            contains = !RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .get("home page with specific units")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .xpath(String.format("/page/units/unit[name='%s']", key))
                .isEmpty();
        }
        return contains;
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
        return new RestUnit(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .get("home page with specific units")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .xpath(String.format("/page/units/unit[name='%s']/links/link[@rel='edit']/@href", key))
                .get(0),
            this.cookie
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit put(final String key, final Unit value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit remove(final Object key) {
        RestTester.start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .get("home page with specific units to remove it")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .rel(String.format("/page/units/unit[name='%s']/links/link[@rel='remove']/@href", key))
            .get("remove the unit")
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        return new Unit() {
            @Override
            public SortedMap<Date, Pulse> pulses() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void spec(Spec spec) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Spec spec() {
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
