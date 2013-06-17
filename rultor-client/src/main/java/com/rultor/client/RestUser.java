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
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import javax.validation.constraints.NotNull;
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
public final class RestUser implements User {

    /**
     * Authentication cookie.
     */
    public static final String COOKIE = "X-Rultor-Auth";

    /**
     * Home URI.
     */
    private final transient String home;

    /**
     * Authentication cookie.
     */
    private final transient String cookie;

    /**
     * Public ctor, with custom entry point.
     * @param entry Entry point (URI)
     * @param urn User unique name in the system
     * @param key Secret authentication key
     */
    public RestUser(@NotNull final URI entry,
        @NotNull final URN urn, @NotNull final String key) {
        this.home = entry.toString();
        this.cookie = String.format("%s %s", urn, key);
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
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .header(RestUser.COOKIE, this.cookie)
                .get("read identity URN from home page")
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
    public Map<String, Unit> units() {
        return new RestUnits(this.home, this.cookie);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit create(final String name) {
        return new RestUnit(
            RestTester.start(UriBuilder.fromUri(this.home))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .header(RestUser.COOKIE, this.cookie)
                .get("read front page to get ADD link")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .rel("/page/links/link[@rel='add']/@href")
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .get("read adding form")
                .assertStatus(HttpURLConnection.HTTP_OK)
                .rel("/page/links/link[@rel='save']/@href")
                .post(
                    String.format("name=%s&spec=java.lang.Integer(1)", name),
                    "save skeleton"
                )
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
                .follow()
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .get("read home page again, with unit in it")
                .xpath(
                    String.format(
                        "//unit[name='%s']/links/link[@rel='edit']/@href",
                        name
                    )
                )
                .get(0),
            this.cookie
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String name) {
        RestTester.start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .header(RestUser.COOKIE, this.cookie)
            .get("read list of units to delete one")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/page/units")
            .rel(
                String.format(
                    "//unit[name='%s']/links/link[@rel='remove']/@href",
                    name
                )
            )
            .get("delete selected unit")
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
    }

}
