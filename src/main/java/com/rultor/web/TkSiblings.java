/**
 * Copyright (c) 2009-2018, rultor.com
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
import com.rultor.agents.daemons.Home;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.cactoos.iterable.Limited;
import org.cactoos.list.SolidList;
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
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.50
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
        final SolidList<Talk> siblings = new SolidList<>(
            new Limited<>(
                Tv.TWENTY,
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
     */
    private XeSource more(final String repo, final List<Talk> siblings)
        throws IOException {
        final XeSource src;
        if (siblings.size() == Tv.TWENTY) {
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
     */
    private Iterable<Directive> dirs(final Talk talk) throws IOException {
        final XML xml = talk.read();
        final Directives dirs = new Directives().add("talk").add("archive");
        for (final XML log : xml.nodes("/talk/archive/log")) {
            dirs.append(TkSiblings.log(xml, log));
        }
        return dirs.up().add("name").set(talk.name()).up()
            // @checkstyle MultipleStringLiteralsCheck (1 line)
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
