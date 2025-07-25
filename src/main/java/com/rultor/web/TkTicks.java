/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tick;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
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
 * @since 1.50
 */
final class TkTicks implements Take {

    /**
     * XSLT for pulse render.
     */
    private static final XSL PULSE = XSLDocument.make(
        Objects.requireNonNull(TkTicks.class.getResource("pulse.xsl"))
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
            (Document) TkTicks.PULSE.transform(this.dirs()).inner()
        );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final TranscoderOutput output = new TranscoderOutput(baos);
        final PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_WIDTH, 1_000f
        );
        transcoder.addTranscodingHint(
            PNGTranscoder.KEY_HEIGHT, 100f
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
        for (final Tick tick : this.pulse.ticks()) {
            dirs.add("tick")
                .attr("total", Integer.toString(tick.total()))
                .attr("start", Long.toString(tick.start() - now))
                .attr("msec", Long.toString(tick.duration()))
                .up();
        }
        return new XMLDocument(new Xembler(dirs).xmlQuietly());
    }

}
