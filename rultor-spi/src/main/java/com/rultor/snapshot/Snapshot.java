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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.rexsl.test.SimpleXml;
import com.rexsl.test.XmlDocument;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;
import org.w3c.dom.Document;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;
import org.xembly.XemblySyntaxException;

/**
 * Snapshot.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "directives")
public final class Snapshot {

    /**
     * Xembly directives.
     */
    private final transient Array<Directive> directives;

    /**
     * Public ctor.
     * @param stream Stream to read it from
     * @throws IOException If fails to read
     * @throws XemblySyntaxException If some syntax exception inside
     * @checkstyle ThrowsCount (5 lines)
     * @checkstyle RedundantThrows (5 lines)
     */
    public Snapshot(final InputStream stream)
        throws IOException, XemblySyntaxException {
        this(Snapshot.fetch(stream));
    }

    /**
     * Public ctor.
     * @param script Script
     * @throws XemblySyntaxException If can't parse
     * @checkstyle RedundantThrows (5 lines)
     */
    public Snapshot(final String script) throws XemblySyntaxException {
        this(new Directives(script));
    }

    /**
     * Public ctor.
     * @param dirs Directives
     */
    public Snapshot(final Directives dirs) {
        this.directives = new Array<Directive>(dirs);
    }

    /**
     * Get xembly script.
     * @return The script
     */
    public String xembly() {
        return new Directives(this.directives).toString();
    }

    /**
     * Get empty DOM.
     * @return The DOM
     */
    public static Document empty() {
        final Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        dom.appendChild(dom.createElement("snapshot"));
        return dom;
    }

    /**
     * Make DOM out of it.
     * @return The DOM
     * @throws ImpossibleModificationException If can't apply
     * @checkstyle RedundantThrows (3 lines)
     */
    public Document dom() throws ImpossibleModificationException {
        final Document dom = Snapshot.empty();
        this.apply(dom);
        return dom;
    }

    /**
     * Make XML out of it.
     * @return The XML
     * @throws ImpossibleModificationException If can't apply
     * @checkstyle RedundantThrows (3 lines)
     */
    public XmlDocument xml() throws ImpossibleModificationException {
        return new SimpleXml(new DOMSource(this.dom()));
    }

    /**
     * Apply it to the DOM.
     * @param dom DOM document
     * @throws ImpossibleModificationException If fails at some point
     * @checkstyle RedundantThrows (10 lines)
     */
    @Loggable(
        value = Loggable.DEBUG,
        ignore = ImpossibleModificationException.class
    )
    public void apply(final Document dom)
        throws ImpossibleModificationException {
        new Xembler(this.directives).apply(dom);
    }

    /**
     * Fetch script from the stream.
     * @param stream Input stream where to find details
     * @return The script
     * @throws IOException If IO problem inside
     * @throws XemblySyntaxException If broken syntax
     * @checkstyle ThrowsCount (5 lines)
     * @checkstyle RedundantThrows (4 lines)
     */
    private static String fetch(final InputStream stream)
        throws IOException, XemblySyntaxException {
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, CharEncoding.UTF_8)
        );
        final StringBuilder buf = new StringBuilder();
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (XemblyLine.existsIn(line)) {
                buf.append(XemblyLine.parse(line).xembly());
            }
        }
        if (buf.length() == 0) {
            buf.append("XPATH '/snapshot';");
        }
        return buf.toString();
    }

}
