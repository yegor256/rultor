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
import com.jcabi.immutable.Array;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Snapshot.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class Snapshot {

    /**
     * Details.
     */
    private final transient Array<Detail> details;

    /**
     * Public ctor.
     * @param stream Input stream where to find details
     * @throws IOException If IO problem inside
     */
    public Snapshot(final InputStream stream) throws IOException {
        this.details = new Array<Detail>(Snapshot.fetch(stream));
    }

    /**
     * Print it as an XML document.
     * @return XML
     */
    @NotNull(message = "output XML is never NULL")
    public Document xml() {
        final Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        final Element root = dom.createElement("snapshot");
        dom.adoptNode(root);
        for (Detail detail : this.details) {
            detail.refine(dom);
        }
        return dom;
    }

    /**
     * Fetch them from input stream.
     * @param stream Input stream
     * @return Iterator of details
     * @throws IOException If fails on IO problem
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Collection<Detail> fetch(final InputStream stream)
        throws IOException {
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream)
        );
        final Collection<Detail> details = new LinkedList<Detail>();
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (TextDetail.contains(line)) {
                details.add(new TextDetail(line));
            }
        }
        return details;
    }

}
