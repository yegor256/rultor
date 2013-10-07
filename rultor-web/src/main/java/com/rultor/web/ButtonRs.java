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
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rexsl.test.RestTester;
import com.rexsl.test.XmlDocument;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGSyntax;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * Health button.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/b/stand/{stand:[\\w\\-]+}/{rule:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
public final class ButtonRs extends BaseRs {

    /**
     * Instance that retrieves XML from application.
     */
    private static final ButtonRs.Build DEFAULT_BUILD = new ButtonRs.Build() {
        @Override
        public XmlDocument info(final URI uri, final String stnd) {
            return RestTester.start(
                UriBuilder.fromUri(uri).path(stnd).build()
            )
                .header(
                    HttpHeaders.ACCEPT,
                    MediaType.SVG_UTF_8
                )
                .get("retrieve stand");
        }
    };

    /**
     * Provider of build information.
     */
    private final transient ButtonRs.Build build;

    /**
     * Stand name.
     */
    private transient String stand;

    /**
     * Rule name.
     */
    private transient String rule;

    /**
     * Public constructor.
     */
    public ButtonRs() {
        this(ButtonRs.DEFAULT_BUILD);
    }

    /**
     * Constructor.
     * @param bld Build info retriever.
     */
    public ButtonRs(final ButtonRs.Build bld) {
        super();
        this.build = bld;
    }

    /**
     * Inject it from query.
     * @param stnd Stand name
     */
    @PathParam("stand")
    public void setStand(@NotNull(message = "stand name can't be NULL")
        final String stnd) {
        this.stand = stnd;
    }

    /**
     * Inject it from query.
     * @param rle Rule name
     */
    @PathParam("rule")
    public void setRule(@NotNull(message = "rule name can't be NULL")
        final String rle) {
        this.rule = rle;
    }

    /**
     * Draw image with build stats.
     * @return Image generated.
     * @throws Exception In case of problems generating image.
     */
    @GET
    @Path("/")
    @Produces("image/svg+xml")
    public Response button() throws Exception {
        final List<String> health = this.info(
            this.build.info(
                URI.create("http://www.rultor.com/s/"), this.stand
            )
        );
        return Response.ok(draw(health), MediaType.SVG_UTF_8.toString())
            .build();
    }

    /**
     * Draw build info on image.
     * @param infos Information to draw on the image.
     * @return Image with with info drawn on it.
     * @throws IOException In case of image read/write error.
     */
    private String draw(final List<String> infos) throws IOException {
        final Document document = SVGDOMImplementation.getDOMImplementation()
            .createDocument("http://www.w3.org/2000/svg", "svg", null);
        final SVGGraphics2D svg = new SVGGraphics2D(document);
        final int width = Tv.HUNDRED;
        final int height = Tv.FIFTY;
        svg.setSVGCanvasSize(new Dimension(width, height));
        final int size = Tv.FORTY;
        svg.drawString("R", width - size, height - Tv.SEVEN);
        this.drawString(svg, infos);
        final StringWriter writer = new StringWriter();
        svg.stream(this.root(document, svg, size), writer);
        return writer.toString();
    }

    /**
     * Prepare root element of SVG.
     * @param document SVG document.
     * @param svg SVG graphics.
     * @param size Font size.
     * @return SVG element.
     */
    private SVGSVGElement root(final Document document,
        final SVGGraphics2D svg, final int size) {
        final SVGSVGElement root = (SVGSVGElement) svg.getRoot();
        final Element defs = root.getElementById(
            SVGSyntax.ID_PREFIX_GENERIC_DEFS
        );
        final Element style = document.createElementNS(
            SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STYLE_TAG
        );
        style.setAttributeNS(null, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/css");
        root.setAttribute("font-family", "rultor");
        root.setAttribute("font-size", String.format("%dpx", size));
        final CDATASection styleSheet = document.createCDATASection(
            // @checkstyle StringLiteralsConcatenation (9 lines)
            "@font-face {"
            + "font-family: 'rultor';"
            + "src: url('//img.rultor.com/rultor.eot');"
                //@checkstyle LineLength (1 line)
            + "src: url('//img.rultor.com/rultor.eot?#iefix') format('embedded-opentype'),"
            + "url('//img.rultor.com/rultor.woff') format('woff'),"
            + "url('//img.rultor.com/rultor.ttf') format('truetype'),"
            + "url('//img.rultor.com/rultor.svg?#rultor') format('svg');"
            + "}"
        );
        style.appendChild(styleSheet);
        defs.appendChild(style);
        return root;
    }

    /**
     * Retrieve build related information.
     * @param response Response to parse for the info.
     * @return Retrieved information.
     */
    private List<String> info(final XmlDocument response) {
        final XmlDocument node = response.nodes(
            String.format(
                // @checkstyle LineLength (1 line)
                "/page/widgets/widget[@class='com.rultor.widget.BuildHealth']/builds/build[coordinates/rule='%s'][1]",
                this.rule
            )
        ).get(0);
        final List<String> health = new LinkedList<String>();
        health.add(node.xpath("code/text()").get(0));
        health.add(node.xpath("duration/text()").get(0));
        health.add(node.xpath("health/text()").get(0));
        return health;
    }

    /**
     * Draw lines of text on graphics.
     * @param gfx Graphics to use.
     * @param lines Lines of text.
     */
    private void drawString(final Graphics2D gfx, final List<String> lines) {
        gfx.setFont(new Font("serif", Font.PLAIN, Tv.TEN));
        for (int offset = 0; offset < lines.size(); ++offset) {
            gfx.setPaint(Color.BLACK);
            gfx.drawString(
                lines.get(offset),
                0, gfx.getFontMetrics().getHeight() * (offset + 1)
            );
        }
    }

    /**
     * Retrieves build information.
     */
    public interface Build {
        /**
         * Retrieve build info.
         * @param uri Location to use.
         * @param stand Stand name to use.
         * @return Response.
         */
        XmlDocument info(final URI uri, final String stand);
    }
}
