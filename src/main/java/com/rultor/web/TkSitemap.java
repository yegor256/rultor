/**
 * Copyright (c) 2009-2021 Yegor Bugayenko
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
import com.rultor.Time;
import com.rultor.agents.daemons.Home;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;

/**
 * Sitemap.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.26
 */
final class TkSitemap implements Take {

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Ctor.
     * @param tks Talks
     */
    TkSitemap(final Talks tks) {
        this.talks = tks;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsWithType(
            new RsWithBody(this.xml()),
            "text/xml"
        );
    }

    /**
     * XML.
     * @return XML
     * @throws IOException If fails
     */
    private String xml() throws IOException {
        final StringBuilder doc = new StringBuilder(Tv.THOUSAND).append(
            "<urlset xmlns='http://www.sitemaps.org/schemas/sitemap/0.9'>"
        );
        for (final Talk talk : this.talks.recent()) {
            final XML xml = talk.read();
            for (final String hash : xml.xpath("/talk/archive/log/@id")) {
                doc.append(TkSitemap.toXML(talk, xml, hash));
            }
        }
        return doc.append("</urlset>").toString();
    }

    /**
     * Convert XML and hash into node.
     * @param talk Talk
     * @param xml Talk XML
     * @param hash Hash
     * @return XML text
     * @throws IOException If fails
     */
    private static String toXML(final Talk talk, final XML xml,
        final String hash) throws IOException {
        return new StringBuilder(Tv.HUNDRED)
            .append("<url><loc>")
            .append(
                StringEscapeUtils.escapeXml11(
                    new Home(xml, hash).uri().toString()
                )
            )
            .append("</loc><lastmod>")
            .append(new Time(talk.updated()).iso())
            .append("</lastmod></url>")
            .toString();
    }

}
