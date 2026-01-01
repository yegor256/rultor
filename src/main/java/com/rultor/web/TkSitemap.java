/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.xml.XML;
import com.rultor.Time;
import com.rultor.agents.daemons.Home;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import org.apache.commons.text.StringEscapeUtils;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;

/**
 * Sitemap.
 *
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
        final StringBuilder doc = new StringBuilder(1_000).append(
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
     * @checkstyle AbbreviationAsWordInNameCheck (10 lines)
     */
    private static String toXML(final Talk talk, final XML xml,
        final String hash) throws IOException {
        return new StringBuilder(100)
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
