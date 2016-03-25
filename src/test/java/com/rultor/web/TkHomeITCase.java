/**
 * Copyright (c) 2009-2016, rultor.com
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

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import java.net.HttpURLConnection;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration case for {@link HomeRs}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.5
 */
public final class TkHomeITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("takes.home");

    /**
     * Before the entire test.
     */
    @BeforeClass
    public static void before() {
        Assume.assumeNotNull(TkHomeITCase.HOME);
    }

    /**
     * IndexRs can render absent pages.
     * @throws Exception If some problem inside
     */
    @Test
    @Ignore
    public void renderAbsentPages() throws Exception {
        final String[] pages = {
            "/page-doesnt-exist",
            "/xsl/xsl-stylesheet-doesnt-exist.xsl",
            "/css/stylesheet-is-absent.css",
        };
        final Request request = new JdkRequest(TkHomeITCase.HOME);
        for (final String page : pages) {
            request.uri().path(page).back()
                .method(Request.GET)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
                .as(XmlResponse.class)
                .assertXPath("//xhtml:title[contains(.,'page not found')]");
        }
    }

    /**
     * IndexRs can render valid pages.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersValidPages() throws Exception {
        final String[] pages = {
            "/robots.txt",
            "/xsl/layout.xsl",
            "/xsl/home.xsl",
            "/css/style.css",
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
    public void showsVersion() throws Exception {
        new JdkRequest(TkHomeITCase.HOME)
            .uri().path("/").back()
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/page/version/name");
    }

}
