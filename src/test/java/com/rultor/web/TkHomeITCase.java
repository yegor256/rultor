/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import javax.imageio.ImageIO;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration case for {@link TkHome}.
 * @since 0.5
 */
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
final class TkHomeITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("takes.home");

    /**
     * Before the entire test.
     */
    @BeforeEach
    void before() {
        Assumptions.assumeFalse(System.getProperty("takes.port").isEmpty());
        Assumptions.assumeFalse(TkHomeITCase.HOME == null);
        Assumptions.assumeFalse(TkHomeITCase.HOME.isEmpty());
    }

    /**
     * IndexRs can render absent pages.
     * @throws Exception If some problem inside
     */
    @Test
    void renderAbsentPages() throws Exception {
        final String[] pages = {
            "/xsl/xsl-stylesheet-doesnt-exist.xsl",
            "/css/stylesheet-is-absent.css",
        };
        final Request request = new JdkRequest(TkHomeITCase.HOME);
        for (final String page : pages) {
            request.uri().path(page).back()
                .method(Request.GET)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /**
     * Redirects from absent URL.
     * @throws Exception If some problem inside
     */
    @Test
    void redirectsOnAbsence() throws Exception {
        final String[] pages = {
            "/page-doesnt-exist",
            "/oops",
        };
        final Request request = new JdkRequest(TkHomeITCase.HOME);
        for (final String page : pages) {
            request.uri().path(page).back()
                .method(Request.GET)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        }
    }

    /**
     * IndexRs can render valid pages.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersValidPages() throws Exception {
        final String[] pages = {
            "/robots.txt",
            "/xsl/layout.xsl",
            "/xsl/home.xsl",
            "/css/main.css",
            "/css/siblings.css",
        };
        final Request request = new JdkRequest(TkHomeITCase.HOME);
        for (final String page : pages) {
            request.uri().path(page).back()
                .method(Request.GET)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
    }

    /**
     * IndexRs can show version.
     * @throws Exception If some problem inside
     */
    @Test
    void showsVersion() throws Exception {
        new JdkRequest(TkHomeITCase.HOME)
            .uri().path("/").back()
            .header("Accept", "application/xml")
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/page/version/name");
    }

    /**
     * Renders valid PNG image.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersValidPngTick() throws Exception {
        final Request request = new JdkRequest(TkHomeITCase.HOME);
        final byte[] data = request.uri().path("/ticks").back()
            .method(Request.GET)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .binary();
        MatcherAssert.assertThat(
            "Tick HTTP GET response should return valid png image",
            ImageIO.read(new ByteArrayInputStream(data)).getWidth(),
            Matchers.equalTo(1_000)
        );
    }

}
