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
package com.rultor.snapshot;

import com.jcabi.aspects.Loggable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xembly.ImpossibleModificationException;

/**
 * XSLT post processor of a snapshot.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.DEBUG)
public final class XSLT {

    /**
     * Factory.
     */
    private static final TransformerFactory FACTORY =
        TransformerFactory.newInstance();

    /**
     * Document Factory.
     */
    private static final DocumentBuilderFactory DFACTORY =
        DocumentBuilderFactory.newInstance();

    /**
     * Source.
     */
    private final transient Source source;

    /**
     * XSL.
     */
    private final transient Source xsl;

    /**
     * Ctor.
     * @param snapshot Snapshot
     * @param text XSL as text
     * @throws ImpossibleModificationException If can't build
     * @checkstyle RedundantThrows (5 lines)
     */
    public XSLT(final Snapshot snapshot, final String text)
        throws ImpossibleModificationException {
        this(snapshot, IOUtils.toInputStream(text, Charsets.UTF_8));
    }

    /**
     * Ctor.
     * @param snapshot Snapshot
     * @param stream XSL
     * @throws ImpossibleModificationException If can't build
     * @checkstyle RedundantThrows (5 lines)
     */
    public XSLT(final Snapshot snapshot, final InputStream stream)
        throws ImpossibleModificationException {
        this(new DOMSource(snapshot.dom()), new StreamSource(stream));
    }

    /**
     * Ctor.
     * @param dom DOM source
     * @param stream XSL
     * @checkstyle RedundantThrows (5 lines)
     */
    public XSLT(final Node dom, final InputStream stream) {
        this(new DOMSource(dom), new StreamSource(stream));
    }

    /**
     * Ctor.
     * @param src Source
     * @param style Stylesheet
     */
    public XSLT(final Source src, final Source style) {
        this.source = src;
        this.xsl = style;
    }

    /**
     * Get new document.
     * @return DOM
     * @throws TransformerException If fails
     */
    public Document dom() throws TransformerException {
        final Transformer trans = XSLT.FACTORY.newTransformer(this.xsl);
        final Document dom;
        try {
            dom = XSLT.DFACTORY.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        trans.transform(this.source, new DOMResult(dom));
        return dom;
    }

    /**
     * Get XML.
     * @return XML
     * @throws TransformerException If fails
     */
    public String xml() throws TransformerException {
        final Transformer trans = XSLT.FACTORY.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.METHOD, "xml");
        trans.setOutputProperty(OutputKeys.ENCODING, CharEncoding.UTF_8);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            trans.transform(
                new DOMSource(this.dom()),
                new StreamResult(
                    new OutputStreamWriter(output, CharEncoding.UTF_8)
                )
            );
            return output.toString(CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException ex) {
            throw new TransformerException(ex);
        }
    }

}
