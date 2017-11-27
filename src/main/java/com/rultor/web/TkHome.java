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
import com.rultor.Toggles;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import org.cactoos.iterable.Limited;
import org.ocpsoft.prettytime.PrettyTime;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.50
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
            new XeSource() {
                @Override
                public Iterable<Directive> toXembly() throws IOException {
                    return TkHome.this.recent();
                }
            },
            new XeLink("status", "/status"),
            new XeLink("ticks", "/ticks", "image/png"),
            new XeSource() {
                @Override
                public Iterable<Directive> toXembly() throws IOException {
                    return TkHome.this.flags(req);
                }
            }
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
        for (final Talk talk : new Limited<>(Tv.FIVE, this.talks.recent())) {
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
