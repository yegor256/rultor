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
import com.rexsl.test.JdkRequest;
import com.rexsl.test.Request;
import com.rexsl.test.RestResponse;
import com.rexsl.test.XmlResponse;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Rule;
import com.rultor.spi.Spec;
import com.rultor.spi.Wallet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * RESTful Rule.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "home", "token" })
@Loggable(Loggable.DEBUG)
final class RestRule implements Rule {

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
    protected RestRule(final String uri, final String auth) {
        this.home = uri;
        this.token = auth;
    }

    @Override
    public void update(final Spec spec, final Spec drain) {
        try {
            new JdkRequest(this.home)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, this.token)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .rel("/page/links/link[@rel='save']/@href")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .method(Request.POST)
                .body()
                .formParam("spec", spec.asText())
                .formParam("drain", drain.asText())
                .back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Spec spec() {
        try {
            return new Spec.Simple(
                new JdkRequest(this.home)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                    .header(HttpHeaders.AUTHORIZATION, this.token)
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .as(XmlResponse.class)
                    .xml()
                    .xpath("/page/rule/spec/text()")
                    .get(0)
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String name() {
        try {
            return new JdkRequest(this.home)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, this.token)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .xml()
                .xpath("/page/rule/name/text()")
                .get(0);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Wallet wallet(final Coordinates work, final URN urn,
        final String rule) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spec drain() {
        try {
            return new Spec.Simple(
                new JdkRequest(this.home)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                    .header(HttpHeaders.AUTHORIZATION, this.token)
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .as(XmlResponse.class)
                    .xml()
                    .xpath("/page/rule/drain/text()")
                    .get(0)
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void failure(final String desc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String failure() {
        throw new UnsupportedOperationException();
    }

}
