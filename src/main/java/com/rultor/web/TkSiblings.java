/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.xml.XML;
import com.rultor.agents.daemons.Home;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.cactoos.iterable.HeadOf;
import org.cactoos.list.ListOf;
import org.ocpsoft.prettytime.PrettyTime;
import org.takes.Response;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHref;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Siblings.
 *
 * @since 1.50
 */
final class TkSiblings implements TkRegex {

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Ctor.
     * @param tks Talks
     */
    TkSiblings(final Talks tks) {
        this.talks = tks;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Date since = new Date(
            Long.parseLong(
                new RqHref.Smart(new RqHref.Base(req)).single(
                    "s", Long.toString(Long.MAX_VALUE)
                )
            )
        );
        final String repo = req.matcher().group(1);
        final List<Talk> siblings = new ListOf<>(
            new HeadOf<>(
                20,
                this.talks.siblings(repo, since)
            )
        );
        if (!siblings.isEmpty()
            && !new RqUser(req).canSee(siblings.get(0))) {
            throw new RsForward(
                new RsFlash(
                    "according to .rultor.yml, you're not allowed to see this",
                    Level.WARNING
                )
            );
        }
        return new RsPage(
            "/xsl/siblings.xsl",
            req,
            new XeAppend("repo", repo),
            new XeAppend("since", Long.toString(since.getTime())),
            this.more(repo, siblings),
            new XeDirectives(this.list(siblings))
        );
    }

    /**
     * Link to more, if necessary.
     * @param repo Repo name
     * @param siblings Siblings
     * @return Link or empty
     * @throws IOException If fails
     * @checkstyle NonStaticMethodCheck (10 lines)
     */
    private XeSource more(final String repo, final List<Talk> siblings)
        throws IOException {
        final XeSource src;
        if (siblings.size() == 20) {
            final Talk last = siblings.get(siblings.size() - 1);
            src = new XeLink(
                "more",
                String.format(
                    "/p/%s?s=%s",
                    repo, last.updated().getTime()
                )
            );
        } else {
            src = XeSource.EMPTY;
        }
        return src;
    }

    /**
     * List of directives.
     * @param siblings List of them
     * @return The directives
     * @throws IOException If fails
     */
    private Iterable<Directive> list(final Iterable<Talk> siblings)
        throws IOException {
        final Directives dirs = new Directives().add("siblings");
        for (final Talk talk : siblings) {
            dirs.append(this.dirs(talk));
        }
        return dirs;
    }

    /**
     * Convert talk to directives.
     * @param talk The talk to convert
     * @return Directives
     * @throws IOException If fails
     * @checkstyle NonStaticMethodCheck (10 lines)
     */
    private Iterable<Directive> dirs(final Talk talk) throws IOException {
        final XML xml = talk.read();
        final Directives dirs = new Directives().add("talk").add("archive");
        for (final XML log : xml.nodes("/talk/archive/log")) {
            dirs.append(TkSiblings.log(xml, log));
        }
        return dirs.up().add("name").set(talk.name()).up()
            .add("href").set(xml.xpath("/talk/wire/href/text()").get(0)).up()
            .add("updated").set(Long.toString(talk.updated().getTime())).up()
            .add("timeago").set(new PrettyTime().format(talk.updated())).up()
            .up();
    }

    /**
     * Convert log to JAXB.
     * @param talk Talk
     * @param log The log to convert
     * @return JAXB
     */
    private static Iterable<Directive> log(final XML talk, final XML log) {
        final String hash = log.xpath("@id").get(0);
        return new Directives().add("log")
            .add("id").set(hash).up()
            .add("href").set(new Home(talk, hash).uri().toString()).up()
            .add("title").set(log.xpath("@title").get(0)).up()
            .up();
    }

}
