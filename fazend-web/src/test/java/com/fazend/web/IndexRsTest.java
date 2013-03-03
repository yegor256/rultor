/**
 * Copyright (c) 2011-2013, ReXSL.com
 * All rights reserved.
 */
package com.fazend.web;

import com.rexsl.page.HttpHeadersMocker;
import com.rexsl.page.UriInfoMocker;
import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link IndexRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id: IndexRsTest.java 2344 2013-01-13 18:28:44Z guard $
 */
public final class IndexRsTest {

    /**
     * IndexRs can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersFrontPage() throws Exception {
        final IndexRs res = new IndexRs();
        res.setUriInfo(new UriInfoMocker().mock());
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        final Response response = res.index();
        MatcherAssert.assertThat(
            JaxbConverter.the(response.getEntity()),
            XhtmlMatchers.hasXPaths(
                "/page/message[.='Hello, world!']",
                "/page/version[name='1.0-SNAPSHOT']"
            )
        );
    }

}
