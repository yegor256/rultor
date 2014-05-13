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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.w3c.dom.Document;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.SyntaxException;
import org.xembly.Xembler;

/**
 * Snapshot.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(of = "directives")
public final class Snapshot {

    /**
     * Xembly directives.
     */
    private final transient Directives directives;

    /**
     * Public ctor.
     * @param stream Stream to read it from
     * @throws IOException If fails to read
     * @throws SyntaxException If some syntax exception inside
     * @checkstyle ThrowsCount (5 lines)
     */
    public Snapshot(final InputStream stream)
        throws IOException, SyntaxException {
        this(Snapshot.fetch(stream));
    }

    /**
     * Public ctor.
     * @param script Script
     * @throws SyntaxException If can't parse
     */
    public Snapshot(final String script) throws SyntaxException {
        this(new Directives(script));
    }

    /**
     * Public ctor.
     * @param dirs Directives
     */
    public Snapshot(final Iterable<Directive> dirs) {
        this.directives = new Directives().add("snapshot").append(dirs);
    }

    /**
     * Get xembly script.
     * @return The script
     */
    public String xembly() {
        return new Directives(this.directives).toString();
    }

    /**
     * Make XML out of it.
     * @return The XML
     * @throws XemblyException If can't apply
     * @checkstyle RedundantThrowsCheck (5 lines)
     */
    public XML xml() throws XemblyException {
        final Document dom;
        try {
            dom = new Xembler(this.directives).dom();
        } catch (final ImpossibleModificationException ex) {
            throw new XemblyException(ex);
        }
        final InputStream xsl = this.getClass().getResourceAsStream(
            "remove-duplicate-tags.xsl"
        );
        try {
            return new XMLDocument(new DOMSource(new XSLT(dom, xsl).dom()));
        } catch (final TransformerException ex) {
            throw new XemblyException(ex);
        } finally {
            IOUtils.closeQuietly(xsl);
        }
    }

    /**
     * Get all tags.
     * @return Collection of tags found
     * @throws XemblyException If fails
     * @checkstyle RedundantThrowsCheck (5 lines)
     */
    public Collection<Tag> tags() throws XemblyException {
        final Collection<XML> nodes = this.xml()
            .nodes("/snapshot/tags/tag");
        final Collection<Tag> tags = new ArrayList<Tag>(nodes.size());
        for (final XML node : nodes) {
            tags.add(this.tag(node));
        }
        return Collections.unmodifiableCollection(tags);
    }

    /**
     * Make tag from XML node.
     * @param node The node
     * @return Tag made
     */
    public Tag tag(final XML node) {
        final Level level;
        if (node.nodes("level").isEmpty()) {
            level = Level.INFO;
        } else {
            level = Level.parse(node.xpath("level/text()").get(0));
        }
        final String markdown;
        if (node.nodes("markdown[.!='']").isEmpty()) {
            markdown = "";
        } else {
            markdown = node.xpath("markdown/text()").get(0);
        }
        final ImmutableMap.Builder<String, String> attrs =
            new ImmutableMap.Builder<String, String>();
        for (final XML attr : node.nodes("attributes/attribute")) {
            attrs.put(
                attr.xpath("name/text()").get(0),
                attr.xpath("value/text()").get(0)
            );
        }
        return new Tag.Simple(
            node.xpath("label/text()").get(0),
            level,
            attrs.build(),
            markdown
        );
    }

    /**
     * Fetch script from the stream.
     * @param stream Input stream where to find details
     * @return The script
     * @throws IOException If IO problem inside
     * @throws SyntaxException If broken syntax
     * @checkstyle ThrowsCount (5 lines)
     */
    private static String fetch(final InputStream stream)
        throws IOException, SyntaxException {
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, CharEncoding.UTF_8)
        );
        try {
            final StringBuilder buf = new StringBuilder(Tv.THOUSAND);
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
        } finally {
            reader.close();
        }
    }

}
