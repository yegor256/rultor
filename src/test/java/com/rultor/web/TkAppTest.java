/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
        MatcherAssert.assertThat(
            "Page should be xml document",
            TkAppTest.homePage(), Matchers.startsWith("<?xml ")
        );
    }

    /**
     * App home page contains required XML elements.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersHomePageWithXmlContent() throws Exception {
        MatcherAssert.assertThat(
            "Xml document should contain some data",
            XhtmlMatchers.xhtml(TkAppTest.homePage()),
            XhtmlMatchers.hasXPaths(
                "/page/millis",
                "/page/links/link[@rel='ticks']",
                "/page/toggles[read-only='false']"
            )
        );
    }

    /**
     * Render the home page body.
     * @return Rendered body
     * @throws Exception If some problem inside
     */
    private static String homePage() throws Exception {
        return new TextOf(
            new RsPrint(
                new TkApp(
                    new Talks.InDir(), Pulse.EMPTY,
                    new Toggles.InFile()
                ).act(
                    new RqWithHeader(
                        new RqFake("GET", "/"),
                        "Accept",
                        "text/xml"
                    )
                )
            ).body()
        ).asString();
    }

    /**
     * App can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersHomePageViaHttp() throws Exception {
        Assertions.assertDoesNotThrow(
            () -> new FtRemote(
                new TkApp(
                    new Talks.InDir(), Pulse.EMPTY,
                    new Toggles.InFile()
                )
            ).exec(
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
                    new TkApp(
                        new Talks.InDir(), Pulse.EMPTY,
                        new Toggles.InFile()
                    ).act(
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
        MatcherAssert.assertThat(
            "Page can be gzip compressed",
            new TextOf(
                new GZIPInputStream(
                    new RsPrint(
                        new TkApp(
                            new Talks.InDir(), Pulse.EMPTY,
                            new Toggles.InFile()
                        ).act(
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
