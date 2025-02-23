/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.xml.XML;
import com.rultor.Toggles;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import org.cactoos.iterable.HeadOf;
import org.ocpsoft.prettytime.PrettyTime;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeLink;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Index resource, front page of the website.
 *
 * @since 1.50
 */
final class TkHome implements Take {

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Toggles.
     */
    private final transient Toggles toggles;

    /**
     * Ctor.
     * @param tlks Talks
     * @param tgls Toggles
     */
    TkHome(final Talks tlks, final Toggles tgls) {
        this.talks = tlks;
        this.toggles = tgls;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            "/xsl/home.xsl",
            req,
            this::recent,
            new XeLink("status", "/status"),
            new XeLink("ticks", "/ticks", "image/png"),
            () -> this.flags(req)
        );
    }

    /**
     * Turn recent talks into directives.
     * @return Directives
     * @throws IOException If fails
     */
    private Directives recent() throws IOException {
        final Directives dirs = new Directives().add("recent");
        final PrettyTime pretty = new PrettyTime();
        for (final Talk talk : new HeadOf<>(5, this.talks.recent())) {
            dirs.add("talk").set(talk.name())
                .attr("timeago", pretty.format(talk.updated()));
            final XML xml = talk.read();
            if (!xml.nodes("/talk/wire/href").isEmpty()) {
                dirs.attr(
                    "href",
                    talk.read().xpath("/talk/wire/href/text()").get(0)
                );
            }
            dirs.up();
        }
        return dirs;
    }

    /**
     * Flags/toggles to show.
     * @param req Request
     * @return Directives
     * @throws IOException If fails
     */
    private Iterable<Directive> flags(final Request req) throws IOException {
        final Directives dirs = new Directives().add("toggles");
        dirs.add("read-only")
            .set(Boolean.toString(this.toggles.readOnly())).up();
        if (!new RqUser(req).anonymous()) {
            dirs.append(
                new XeLink("sw:read-only", "/toggles/read-only").toXembly()
            );
        }
        return dirs;
    }

}
