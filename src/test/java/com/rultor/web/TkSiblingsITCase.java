/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import java.net.HttpURLConnection;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration case for {@link SiblingsRs}.
 * @since 1.23
 */
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
final class TkSiblingsITCase {

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
        Assumptions.assumeFalse(TkSiblingsITCase.HOME == null);
    }

    /**
     * SiblingsRs can render index page.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersListOfTalks() throws Exception {
        new JdkRequest(TkSiblingsITCase.HOME).uri().path("/p/test/me").back()
            .header("Accept", "application/xml")
            .method(Request.GET)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/page[repo='test/me']")
            .assertXPath("/page/siblings[count(talk)=0]");
    }

}
