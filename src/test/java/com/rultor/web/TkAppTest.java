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

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.VerboseWire;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.Toggles;
import com.rultor.spi.Pulse;
import com.rultor.spi.Talks;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.takes.Take;
import org.takes.http.FtRemote;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rq.RqWithHeaders;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkApp}.
 * @since 1.50
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class TkAppTest {

    /**
     * Make sure it's prepared.
     */
    @BeforeEach
    void resourcesAvailable() {
        Assumptions.assumeFalse(
            TkAppTest.class.getResource("/xsl/home.xsl") == null
        );
    }

    /**
     * App can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersHomePage() throws Exception {
        final Take take = new TkApp(
            new Talks.InDir(), Pulse.EMPTY,
            new Toggles.InFile()
        );
        final String page = new TextOf(
            new RsPrint(
                take.act(
                    new RqWithHeader(
                        new RqFake("GET", "/"),
                        "Accept",
                        "text/xml"
                    )
                )
            ).body()
        ).asString();
        MatcherAssert.assertThat(
            "Page should be xml document",
            page, Matchers.startsWith("<?xml ")
        );
        MatcherAssert.assertThat(
            "Xml document should contain some data",
            XhtmlMatchers.xhtml(page),
            XhtmlMatchers.hasXPaths(
                "/page/millis",
                "/page/links/link[@rel='ticks']",
                "/page/toggles[read-only='false']"
            )
        );
    }

    /**
     * App can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersHomePageViaHttp() throws Exception {
        final Take app = new TkApp(
            new Talks.InDir(), Pulse.EMPTY,
            new Toggles.InFile()
        );
        Assertions.assertDoesNotThrow(
            () -> new FtRemote(app).exec(
                home -> {
                    new JdkRequest(home)
                        .fetch()
                        .as(RestResponse.class)
                        .assertStatus(HttpURLConnection.HTTP_OK)
                        .as(XmlResponse.class)
                        .assertXPath("/xhtml:html")
                        .assertXPath("//xhtml:img[@class='fork-me']");
                    new JdkRequest(home)
                        .through(VerboseWire.class)
                        .header("Accept", "application/xml")
                        .fetch()
                        .as(RestResponse.class)
                        .assertStatus(HttpURLConnection.HTTP_OK)
                        .as(XmlResponse.class)
                        .assertXPath("/page/version");
                }
            )
        );
    }

    /**
     * App can serve home js.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersHomeJs() throws Exception {
        final Take take = new TkApp(
            new Talks.InDir(), Pulse.EMPTY,
            new Toggles.InFile()
        );
        Assertions.assertEquals(
            new StringBuilder()
                .append("$(document).ready(function(){var a=$(\"#pulse\");")
                .append("window.setInterval(function(){a.find(\"img\")")
                .append(".attr(\"src\",a.attr(\"data-href\")+\"?\"")
                .append("+Date.now())},1E3)});")
                .append('\n')
                .toString(),
            new TextOf(
                new RsPrint(
                    take.act(
                        new RqWithHeader(
                            new RqFake("GET", "/js/home.js?{version/revision}"),
                            "Accept",
                            "text/javascript"
                        )
                    )
                ).body()
            ).asString()
        );
    }

    /**
     * Tests GZIP content return.
     * @throws Exception If fails
     */
    @Test
    @Disabled
    void rendersGzipHomePage() throws Exception {
        final Take take = new TkApp(
            new Talks.InDir(), Pulse.EMPTY,
            new Toggles.InFile()
        );
        MatcherAssert.assertThat(
            "Page can be gzip compressed",
            new TextOf(
                new GZIPInputStream(
                    new RsPrint(
                        take.act(
                            new RqWithHeaders(
                                new RqFake("GET", "/"),
                                "Accept: plain/html",
                                "Accept-Encoding: gzip"
                            )
                        )
                    ).body()
                )
            ).asString(),
            Matchers.startsWith("<!DOCTYPE html")
        );
    }
}
