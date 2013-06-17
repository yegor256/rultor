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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.SortedMap;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * RESTful Unit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "home", "cookie" })
@Loggable(Loggable.DEBUG)
final class RestUnit implements Unit {

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
    protected RestUnit(final String uri, final String auth) {
        this.home = uri;
        this.cookie = auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<Date, Pulse> pulses() {
        return new RestPulses(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .header(RestUser.COOKIE, this.cookie)
                .get("home page with links")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .xpath("/page/links/link[@rel='pulses']/@href")
                .get(0),
            this.cookie
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void spec(final Spec spec) {
        try {
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .header(RestUser.COOKIE, this.cookie)
                .get("home page with save link")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .rel("/page/links/link[@rel='save']/@href")
                .post(
                    String.format(
                        "spec=%s",
                        URLEncoder.encode(spec.asText(), CharEncoding.UTF_8)
                    ),
                    "save spec"
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
    public Spec spec() {
        return new Spec.Simple(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .header(RestUser.COOKIE, this.cookie)
                .get("home page with spec")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .xpath("/page/unit/spec/text()")
                .get(0)
        );
    }

}
