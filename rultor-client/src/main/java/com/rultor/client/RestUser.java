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
import com.rexsl.test.RestResponse;
import com.rexsl.test.XmlResponse;
import com.rultor.spi.Account;
import com.rultor.spi.Rules;
import com.rultor.spi.Stands;
import com.rultor.spi.User;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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
    public RestUser(@NotNull(message = "URI can't be NULL") final URI entry,
        @NotNull(message = "URN can't be NULL") final URN urn,
        @NotNull(message = "key can't be NULL") final String key) {
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
    public RestUser(final URN urn, final String key) {
        this(URI.create("http://www.rultor.com"), urn, key);
    }

    @Override
    public URN urn() {
        try {
            return URN.create(
                new JdkRequest(this.home)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                    .header(HttpHeaders.AUTHORIZATION, this.token)
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .as(XmlResponse.class)
                    .assertXPath("/page/identity")
                    .xml()
                    .xpath("/page/identity/urn/text()")
                    .get(0)
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Rules rules() {
        try {
            return new RestRules(
                URI.create(
                    new JdkRequest(this.home)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                        .header(HttpHeaders.AUTHORIZATION, this.token)
                        .fetch()
                        .as(RestResponse.class)
                        .assertStatus(HttpURLConnection.HTTP_OK)
                        .as(XmlResponse.class)
                        .assertXPath("/page/links/link[@rel='rules']")
                        .xml()
                        .xpath("/page/links/link[@rel='rules']/@href")
                        .get(0)
                ),
                this.token
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Stands stands() {
        try {
            return new RestStands(
                URI.create(
                    new JdkRequest(this.home)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                        .header(HttpHeaders.AUTHORIZATION, this.token)
                        .fetch()
                        .as(RestResponse.class)
                        .assertStatus(HttpURLConnection.HTTP_OK)
                        .as(XmlResponse.class)
                        .assertXPath("/page/links/link[@rel='stands']")
                        .xml()
                        .xpath("/page/links/link[@rel='stands']/@href")
                        .get(0)
                ),
                this.token
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Account account() {
        return new RestAccount(URI.create(this.home), this.token);
    }

}
