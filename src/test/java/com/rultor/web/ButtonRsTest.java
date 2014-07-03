/**
 * Copyright (c) 2009-2014, rultor.com
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
import com.rexsl.mock.HttpHeadersMocker;
import com.rexsl.mock.UriInfoMocker;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletContext;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link ButtonRs}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
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
                    return ButtonRsTest.this.page(rule, "1", "2", "3");
                }
            }
        );
        this.prepare(res);
        MatcherAssert.assertThat(
            URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream(
                    (byte[]) res.pngButton(rule).getEntity()
                )
            ),
            Matchers.equalTo(MediaType.PNG.toString())
        );
    }

    /**
     * ButtonRs can build SVG image.
     * @throws Exception In case of error.
     */
    @Test
    public void buildBasicSvgImage() throws Exception {
        final String rule = "rultor-on-commit-svg";
        final ButtonRs res = new ButtonRs(
            new ButtonRs.Build() {
                @Override
                public String info(final URI uri) {
                    return ButtonRsTest.this.page(rule, "4", "5", "6");
                }
            }
        );
        this.prepare(res);
        MatcherAssert.assertThat(
            (String) res.svgButton(rule).getEntity(),
            Matchers.startsWith("<svg")
        );
    }

    /**
     * ButtonRs can produce image even when missing build information.
     *
     * @throws Exception In case of error.
     */
    @Test
    public void buildImageWithEmptyBuildInfo() throws Exception {
        final ButtonRs res = new ButtonRs(
            new ButtonRs.Build() {
                @Override
                public String info(final URI uri) {
                    return "";
                }
            }
        );
        this.prepare(res);
        MatcherAssert.assertThat(
            URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream(
                    (byte[]) res.pngButton("foo-rule").getEntity()
                )
            ),
            Matchers.equalTo(MediaType.PNG.toString())
        );
    }

    /**
     * ButtonRs can produce image even when missing build information.
     *
     * @throws Exception In case of error.
     */
    @Test
    public void buildImageWhenMissingBuildInfo() throws Exception {
        final ButtonRs res = new ButtonRs(
            new ButtonRs.Build() {
                @Override
                public String info(final URI uri) {
                    return "<page><widgets></widgets></page>";
                }
            }
        );
        this.prepare(res);
        MatcherAssert.assertThat(
            URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream(
                    (byte[]) res.pngButton("bar-rule").getEntity()
                )
            ),
            Matchers.equalTo(MediaType.PNG.toString())
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
                    return ButtonRsTest.this.page(rule, "7", "8", "9");
                }
            }
        );
        res.setUriInfo(
            new UriInfoMocker().withBaseUri(URI.create(base)).mock()
        );
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setStand(stand);
        this.context(res);
        res.pngButton(rule);
        MatcherAssert.assertThat(called.get(), Matchers.is(true));
    }

    /**
     * BuildRs should create image with correct fonts.
     * @throws Exception In case of error.
     * @todo #439 Batik image generation depends on JDK used and in some cases
     *  leads to different images for the same data (e.g. different
     *  anti-aliasing in images) as a result the images can't be compared byte
     *  by byte. This problem can be recreated by running image generation with
     *  OpenJDK and Oracle JDK. Possible solution would be to use vector images
     *  or a different library to generate images.
     */
    @Test
    @Ignore
    public void buildImageWithCorrectFont() throws Exception {
        final String rule = "other-rule";
        final ButtonRs res = new ButtonRs(
            new ButtonRs.Build() {
                @Override
                public String info(final URI uri) {
                    return ButtonRsTest.this.page(
                        rule,
                        StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY
                    );
                }
            }
        );
        this.prepare(res);
        MatcherAssert.assertThat(
            Arrays.equals(
                (byte[]) res.pngButton(rule).getEntity(),
                IOUtils.toByteArray(
                    this.getClass().getResourceAsStream("build.png")
                )
            ),
            Matchers.equalTo(true)
        );
    }

    /**
     * Premare mocks for tests.
     * @param res Button
     */
    private void prepare(final ButtonRs res) {
        res.setUriInfo(new UriInfoMocker().mock());
        res.setHttpHeaders(new HttpHeadersMocker().mock());
        res.setSecurityContext(Mockito.mock(SecurityContext.class));
        res.setStand("stand");
        this.context(res);
    }

    /**
     * Generate build health page.
     * @param rule Rule to use in page.
     * @param duration Duration
     * @param code Code
     * @param health Health
     * @return Page source.
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    private String page(final String rule, final String duration,
        final String code, final String health) {
        return String.format(
            StringUtils.join(
                "<page><widgets><widget ",
                "class=\"com.rultor.widget.BuildHealth\"><builds><build>",
                "  <coordinates><rule>%s</rule></coordinates>",
                "  <duration>%s</duration>",
                "  <code>%s</code>",
                "  <health>%s</health>",
                "</build></builds></widget></widgets></page>"
            ),
            rule, duration, code, health
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

}
