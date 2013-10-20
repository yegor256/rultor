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
package com.rultor.web;

import com.google.common.net.MediaType;
import com.rexsl.page.HttpHeadersMocker;
import com.rexsl.page.UriInfoMocker;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletContext;
import javax.ws.rs.core.SecurityContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link ButtonRs}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class ButtonRsTest {

    /**
     * ButtonRs can build PNG image.
     * @throws Exception In case of error.
     */
    @Test
    public void buildBasicImage() throws Exception {
        final String rule = "rultor-on-commit";
        final ButtonRs res = new ButtonRs(
            new ButtonRs.Build() {
                @Override
                public String info(final URI uri) {
                    return ButtonRsTest.this.page(rule);
                }
            }
        );
        res.setUriInfo(new UriInfoMocker().mock());
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setSecurityContext(Mockito.mock(SecurityContext.class));
        res.setStand("stand");
        res.setRule(rule);
        context(res);
        MatcherAssert.assertThat(
            URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream((byte[]) res.button().getEntity())
            ),
            Matchers.equalTo(MediaType.PNG.toString())
        );
    }

    /**
     * Setup servlet context.
     * @param res Page to setup the context.
     */
    private void context(final ButtonRs res) {
        final ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.when(context.getResourceAsStream(Mockito.anyString()))
            .thenReturn(
                this.getClass().getResourceAsStream(
                    "button.xsl"
                )
            );
        res.setServletContext(context);
    }

    /**
     * Generate build health page.
     * @param rule Rule to use in page.
     * @return Page source.
     */
    private String page(final String rule) {
        return String.format(
            // @checkstyle StringLiteralsConcatenation (8 lines)
            // @checkstyle LineLength (1 line)
            "<page><widgets><widget class=\"com.rultor.widget.BuildHealth\"><builds><build>"
                + "  <coordinates><rule>%s</rule></coordinates>"
                + "  <duration>1212602</duration>"
                + "  <code>0</code>"
                + "  <health>0.6153846153846154</health>"
                + "</build></builds></widget></widgets></page>",
            rule
        );
    }

    /**
     * ButtonRs calls correct URL for image data.
     * @throws Exception In case of error.
     */
    @Test
    public void getsDataFromAppropriateUrl() throws Exception {
        final String stand = "stnd";
        final String base = "http://localhost";
        final String rule = "rule";
        final AtomicBoolean called = new AtomicBoolean(false);
        final ButtonRs res = new ButtonRs(
            new ButtonRs.Build() {
                @Override
                public String info(final URI uri) {
                    MatcherAssert.assertThat(
                        uri.toString(),
                        Matchers.equalTo(
                            String.format("%s/s/%s", base, stand)
                        )
                    );
                    called.set(true);
                    return ButtonRsTest.this.page(rule);
                }
            }
        );
        res.setUriInfo(
            new UriInfoMocker().withBaseUri(URI.create(base)).mock()
        );
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setStand(stand);
        res.setRule(rule);
        this.context(res);
        res.button();
        MatcherAssert.assertThat(called.get(), Matchers.is(true));
    }
}
