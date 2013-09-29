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
import com.rexsl.test.RestTester;
import com.rexsl.test.SimpleXml;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

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
     * Stand name.
     */
    private transient String stand;

    /**
     * Rule name.
     */
    private transient String rule;

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
     */
    @GET
    @Path("/")
    @Produces("image/*")
    public Response button() {
        final String build = RestTester.start(
            UriBuilder.fromUri("http://www.rultor.com/s/").path(this.stand)
                .build()
        )
            .header("Accept", MediaType.APPLICATION_XML_UTF_8.toString())
            .get("retrieve stand")
            .getBody();
        final SimpleXml xml = new SimpleXml(build);
        final String head = String.format(
            // @checkstyle LineLength (1 line)
            "/page/widgets/widget[@class='com.rultor.widget.BuildHealth']/builds/build[coordinates/rule='%s'][1]",
            this.rule
        );
        final String code = xml
            .xpath(String.format("%s/code/text()", head)).get(0);
        final String duration = xml
            .xpath(String.format("%s/duration/text()", head)).get(0);
        final String health = xml
            .xpath(String.format("%s/health/text()", head)).get(0);
        final BufferedImage img;
        try {
            img = ImageIO
                .read(this.getClass().getResourceAsStream("/build_health.png"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        final BufferedImage image = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB
        );
        final Graphics2D gfx = image.createGraphics();
        gfx.drawImage(img, 0, 0, null);
        // @checkstyle MagicNumber (1 line)
        gfx.setFont(new Font("Serif", Font.PLAIN, 10));
        drawString(gfx, Arrays.asList(code, duration, health));
        gfx.dispose();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", stream);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return Response.ok(stream.toByteArray(), MediaType.PNG.toString())
            .build();
    }

    /**
     * Draw lines of text on graphics.
     * @param gfx Graphics to use.
     * @param lines Lines of text.
     */
    private void drawString(final Graphics2D gfx, final List<String> lines) {
        for (int offset = 0; offset < lines.size(); ++offset) {
            gfx.setPaint(Color.BLACK);
            gfx.drawString(
                lines.get(offset),
                0, gfx.getFontMetrics().getHeight() * (offset + 1)
            );
        }
    }
}
