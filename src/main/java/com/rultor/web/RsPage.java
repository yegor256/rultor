/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.rultor.Env;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.auth.XeIdentity;
import org.takes.facets.auth.XeLogoutLink;
import org.takes.facets.auth.social.XeGithubLink;
import org.takes.facets.flash.XeFlash;
import org.takes.rq.RqHeaders;
import org.takes.rs.RsWithType;
import org.takes.rs.RsWrap;
import org.takes.rs.RsXslt;
import org.takes.rs.xe.RsXembly;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDate;
import org.takes.rs.xe.XeLinkHome;
import org.takes.rs.xe.XeLinkSelf;
import org.takes.rs.xe.XeLocalhost;
import org.takes.rs.xe.XeMillis;
import org.takes.rs.xe.XeSla;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeStylesheet;

/**
 * Index resource, front page of the website.
 *
 * @since 1.50
 */
@EqualsAndHashCode(callSuper = true)
final class RsPage extends RsWrap {

    /**
     * Ctor.
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     */
    RsPage(final String xsl, final Request req, final XeSource... src)
        throws IOException {
        super(RsPage.make(xsl, req, src));
    }

    /**
     * Make it.
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @return Response
     * @throws IOException If fails
     */
    private static Response make(final String xsl, final Request req,
        final XeSource... src
    ) throws IOException {
        final Response raw = new RsXembly(
            new XeStylesheet(xsl),
            new XeAppend(
                "page",
                new XeMillis(false),
                new XeChain(src),
                new XeLinkHome(req),
                new XeLinkSelf(req),
                new XeMillis(true),
                new XeDate(),
                new XeSla(),
                new XeLocalhost(),
                new XeIdentity(req),
                new XeFlash(req),
                new XeGithubLink(req, Env.read("Rultor-GithubId")),
                new XeLogoutLink(req),
                new XeAppend(
                    "version",
                    new XeAppend("name", Env.read("Rultor-Version")),
                    new XeAppend("revision", Env.read("Rultor-Revision")),
                    new XeAppend("date", Env.read("Rultor-Date"))
                )
            )
        );
        return RsPage.typedResponse(raw, req);
    }

    /**
     * Build correct response by requested header 'Accept'.
     *
     * @param raw Prepared raw response.
     * @param req Request.
     * @return Correct response according to 'Accept' header.
     * @throws IOException If fails.
     * @todo #1633:90min Replace typedResponse static method with RsFork.
     *  The current solution with typedResponse method is crutch, actually.
     *  The previous solution with {@link org.takes.facets.fork.RsFork} was
     *  broken after the Maven 3.9.0 release. The proper solution would be
     *  to fix the original problem in
     *  <a href="https://github.com/yegor256/takes">takes</a> framework and
     *  then to return the correct solution with
     *  {@link org.takes.facets.fork.RsFork} back.
     */
    private static Response typedResponse(
        final Response raw,
        final Request req
    ) throws IOException {
        final Response resp;
        final Collection<String> headers = new HashSet<>(
            new RqHeaders.Base(req).header("Accept")
        );
        final String xml = "text/xml";
        if (headers.contains("application/xml") || headers.contains(xml)) {
            resp = new RsWithType(raw, xml);
        } else {
            resp = new RsXslt(new RsWithType(raw, "text/html"));
        }
        return resp;
    }
}
