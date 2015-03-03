/**
 * Copyright (c) 2009-2015, rultor.com
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

import com.rultor.agents.daemons.Tail;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsFluent;

/**
 * Single daemon.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.50
 */
final class TkDaemon implements Take {

    /**
     * User.
     */
    private final transient User user;

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Talk unique number.
     */
    private final transient long number;

    /**
     * Daemon hash ID.
     */
    private final transient String hash;

    /**
     * Ctor.
     * @param req Request
     * @param tlks Talks
     * @param num Talk number
     * @param hsh Hash of the daemon
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    TkDaemon(final Request req, final Talks tlks,
        final long num, final String hsh) {
        this.user = new User(req);
        this.talks = tlks;
        this.number = num;
        this.hash = hsh;
    }

    @Override
    public Response act() throws IOException {
        if (!this.talks.exists(this.number)) {
            throw new RsForward(
                new RsFlash(
                    "there is no such page here",
                    Level.WARNING
                )
            );
        }
        if (!this.user.canSee(this.talks.get(this.number))) {
            throw new RsForward(
                new RsFlash(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "according to .rultor.yml, you (%s) are not allowed to see this",
                        this.user
                    ),
                    Level.WARNING
                )
            );
        }
        return new RsFluent()
            .withBody(this.html())
            .withType("text/html; charset=utf-8");
    }

    /**
     * Get HTML.
     * @return HTML
     * @throws IOException If fails
     */
    private InputStream html() throws IOException {
        final Talk talk = this.talks.get(this.number);
        final String head = IOUtils.toString(
            this.getClass().getResourceAsStream("daemon/head.html"),
            CharEncoding.UTF_8
        ).replace("TALK_NAME", talk.name());
        return new SequenceInputStream(
            Collections.enumeration(
                Arrays.asList(
                    IOUtils.toInputStream(head),
                    new Tail(talk.read(), this.hash).read(),
                    this.getClass().getResourceAsStream("daemon/tail.html")
                )
            )
        );
    }

}
