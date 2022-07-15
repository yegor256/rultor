/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tick;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;
import org.w3c.dom.Document;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * PNG with pulse.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.50
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkTicks implements Take {

    /**
     * XSLT for pulse render.
     */
    private static final XSL PULSE = XSLDocument.make(
        TkTicks.class.getResourceAsStream("pulse.xsl")
    );

    /**
     * Pulse.
     */
    private final transient Pulse pulse;

    /**
     * Ctor.
     * @param pls Pulse
     */
    TkTicks(final Pulse pls) {
        this.pulse = pls;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsWithType(
            new RsWithBody(this.png()),
            "image/png"
        );
    }

    /**
     * Make PNG in bytes.
     * @return Bytes
     * @throws IOException If fails
     */
    private byte[] png() throws IOException {
        final TranscoderInput input = new TranscoderInput(
            Document.class.cast(
                TkTicks.PULSE.transform(this.dirs()).node()
            )
        );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final TranscoderOutput output = new TranscoderOutput(baos);
        final PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_WIDTH, (float) Tv.THOUSAND
        );
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_HEIGHT, (float) Tv.HUNDRED
        );
        try {
            transcoder.transcode(input, output);
        } catch (final TranscoderException ex) {
            throw new IOException(ex);
        }
        return baos.toByteArray();
    }

    /**
     * Turn ticks into XML.
     * @return XML
     */
    private XML dirs() {
        final long now = System.currentTimeMillis();
        final Directives dirs = new Directives().add("pulse");
        final Iterator<Tick> iterator = this.pulse.ticks().iterator();
        while (iterator.hasNext()) {
            final Tick tick = iterator.next();
            dirs.add("tick")
                .attr("total", Integer.toString(tick.total()))
                .attr("start", Long.toString(tick.start() - now))
                .attr("msec", Long.toString(tick.duration()))
                .up();
        }
        return new XMLDocument(new Xembler(dirs).xmlQuietly());
    }

}
