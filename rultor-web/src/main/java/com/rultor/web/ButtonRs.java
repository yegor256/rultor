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
import com.rultor.snapshot.XSLT;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.stream.StreamSource;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;

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
    private static final Build DEFAULT_BUILD = new Build() {
        @Override
        public String info(final URI uri) {
            return RestTester.start(uri)
                .header(
                    HttpHeaders.ACCEPT,
                    MediaType.APPLICATION_XML_UTF_8.toString()
                )
                .get("retrieve stand")
                .getBody();
        }
    };

    /**
     * Provider of build information.
     */
    private final transient Build build;

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
    public ButtonRs(final Build bld) {
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
    @Produces("image/png")
    public Response button() throws Exception {
        final PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_WIDTH, (float) Tv.HUNDRED
        );
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_HEIGHT, (float) Tv.FIFTY
        );
        final ByteArrayOutputStream png = new ByteArrayOutputStream();
        transcoder.transcode(
            new TranscoderInput(IOUtils.toInputStream(this.svg())),
            new TranscoderOutput(png)
        );
        return Response
            .ok(png.toByteArray(), MediaType.PNG.toString())
            .build();
    }

    /**
     * Create SVG from build.
     * @return String with SVG.
     * @throws Exception In case of transformation error.
     */
    private String svg() throws Exception {
        return new XSLT(
            new StreamSource(
                IOUtils.toInputStream(
                    this.build.info(
                        UriBuilder.fromUri(this.uriInfo().getBaseUri())
                            .segment("s", this.stand).build()
                    )
                )
            ),
            new StreamSource(
                IOUtils.toInputStream(
                    String.format(
                        IOUtils.toString(
                            this.getClass().getResourceAsStream(
                                "button.xsl"
                            )
                        ),
                        this.rule
                    )
                )
            )
        ).xml();
    }

    /**
     * Retrieves build information.
     */
    public interface Build {
        /**
         * Retrieve build info.
         * @param uri Location to use.
         * @return Response.
         */
        String info(final URI uri);
    }
}
