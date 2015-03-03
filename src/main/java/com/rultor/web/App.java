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

import com.jcabi.manifests.Manifests;
import com.rultor.Toggles;
import com.rultor.spi.Pulse;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Collection;
import org.takes.Request;
import org.takes.Take;
import org.takes.Takes;
import org.takes.f.auth.CcPlain;
import org.takes.f.auth.CcSalted;
import org.takes.f.auth.CcXOR;
import org.takes.f.auth.PsChain;
import org.takes.f.auth.PsCookie;
import org.takes.f.auth.PsGithub;
import org.takes.f.auth.TsAuth;
import org.takes.f.fallback.Fallback;
import org.takes.f.fallback.RqFallback;
import org.takes.f.fallback.TsFallback;
import org.takes.rq.RqRegex;
import org.takes.rs.RsVelocity;
import org.takes.tk.TkFixed;
import org.takes.tk.TkRedirect;
import org.takes.ts.TsRegex;
import org.takes.ts.TsWithHeaders;

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
    private final transient Takes origin;

    /**
     * Ctor.
     * @param talks Talks
     * @param ticks Ticks
     * @param toggles Toggles
     */
    public App(final Talks talks, final Collection<Pulse.Tick> ticks,
        final Toggles toggles) {
        Takes takes = new TsRegex()
            .with("/robots.txt", "")
            .with("/svg", new TkSVG(ticks))
            .with("/s/.*", new TkRedirect("/"))
            .with("/sitemap", new TkSitemap(talks))
            .with(
                "/toggles/read-only",
                new Takes() {
                    @Override
                    public Take route(final Request req) throws IOException {
                        return new TkAdminOnly(new TkToggles(toggles), req);
                    }
                }
            )
            .with(
                "/",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) {
                        return new TkHome(talks, toggles, req);
                    }
                }
            )
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
                "/p/([/a-zA-Z0-9_\\-\\.]+)",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) throws IOException {
                        return new TkSiblings(
                            talks, req.matcher().group(1), req
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
                            talks, req, Long.parseLong(req.matcher().group(1))
                        );
                    }
                }
            )
            .with(
                "/t/([0-9]+)/kill",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) throws IOException {
                        return new TkAdminOnly(
                            new TkTalkKill(
                                talks, Long.parseLong(req.matcher().group(1))
                            ),
                            req
                        );
                    }
                }
            )
            .with(
                "/t/([0-9]+)/delete",
                new TsRegex.Fast() {
                    @Override
                    public Take take(final RqRegex req) throws IOException {
                        return new TkAdminOnly(
                            new TkTalkDelete(
                                talks, Long.parseLong(req.matcher().group(1))
                            ),
                            req
                        );
                    }
                }
            );
        final String rev = Manifests.read("Rultor-Revision");
        takes = new TsFallback(
            new TsWithHeaders(takes)
                .with("Vary", "Cookie")
                .with("X-Rultor-Revision", rev),
            new Fallback() {
                @Override
                public Take take(final RqFallback req) throws IOException {
                    return new TkFixed(
                        new RsVelocity(
                            this.getClass().getResource("/error.html.vm")
                        ).with("req", req).with("rev", rev)
                    );
                }
            }
        );
        takes = new TsAuth(
            takes,
            new PsChain(
                new PsFake(),
                new PsCookie(
                    new CcXOR(
                        new CcSalted(new CcPlain()),
                        Manifests.read("Rultor-SecurityKey")
                    )
                ),
                new PsGithub(
                    Manifests.read("Rultor-GithubId"),
                    Manifests.read("Rultor-GithubSecret")
                )
            )

        );
        this.origin = takes;
    }

    @Override
    public Take route(final Request request) throws IOException {
        return this.origin.route(request);
    }

}
