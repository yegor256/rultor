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

import com.rultor.spi.Pulse;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Collection;
import org.takes.Request;
import org.takes.Take;
import org.takes.Takes;
import org.takes.rq.RqRegex;
import org.takes.tk.TkRedirect;
import org.takes.ts.TsRegex;

/**
 * App.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.50
 */
public final class App implements Takes {

    /**
     * Takes.
     */
    private final transient Takes takes;

    /**
     * Ctor.
     * @param talks Talks
     * @param ticks Ticks
     */
    public App(final Talks talks, final Collection<Pulse.Tick> ticks) {
        this.takes = new TsRegex()
            .with("/robots.txt", "")
            .with("/", new TkHome(talks))
            .with("/svg", new TkSVG(ticks))
            .with("/s/.*", new TkRedirect("/"))
            .with(
                "/b/([/a-zA-Z0-9_\\-\\.]+)",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) {
                        return new TkButton(req.matcher().group(1));
                    }
                }
            )
            .with(
                "/t/([0-9]+)-([a-f0-9]+)",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) {
                        return new TkDaemon(
                            req, talks,
                            Long.parseLong(req.matcher().group(1)),
                            req.matcher().group(1)
                        );
                    }
                }
            )
            .with(
                "/t/([0-9]+)",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) {
                        return new TkTalk(
                            talks, Long.parseLong(req.matcher().group(1))
                        );
                    }
                }
            )
            .with(
                "/t/([0-9]+)/kill",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) {
                        return new TkTalkKill(
                            talks, Long.parseLong(req.matcher().group(1))
                        );
                    }
                }
            )
            .with(
                "/t/([0-9]+)/delete",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) {
                        return new TkTalkDelete(
                            talks, Long.parseLong(req.matcher().group(1))
                        );
                    }
                }
            );
    }

    @Override
    public Take route(final Request request) throws IOException {
        return this.takes.route(request);
    }

}
