/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
import java.nio.charset.Charset;
import org.takes.Take;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkGzip;
import org.takes.tk.TkMeasured;
import org.takes.tk.TkRedirect;
import org.takes.tk.TkVersioned;
import org.takes.tk.TkWithHeaders;
import org.takes.tk.TkWithType;
import org.takes.tk.TkWrap;

/**
 * App.
 *
 * @since 1.50
 */
public final class TkApp extends TkWrap {

    /**
     * Revision of rultor.
     */
    private static final String REV = Manifests.read("Rultor-Revision");

    /**
     * Ctor.
     * @param talks Talks
     * @param pulse Pulse
     * @param toggles Toggles
     */
    public TkApp(final Talks talks, final Pulse pulse,
        final Toggles toggles) {
        super(TkApp.make(talks, pulse, toggles));
    }

    /**
     * Ctor.
     * @param talks Talks
     * @param pulse Pulse
     * @param toggles Toggles
     * @return Takes
     */
    private static Take make(final Talks talks,
        final Pulse pulse, final Toggles toggles) {
        if (!"UTF-8".equals(Charset.defaultCharset().name())) {
            throw new IllegalStateException(
                String.format(
                    "The default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        return new TkWithHeaders(
            new TkVersioned(
                new TkGzip(
                    new TkMeasured(
                        new TkFlash(
                            new TkAppFallback(
                                new TkAppAuth(
                                    new TkForward(
                                        TkApp.regex(talks, pulse, toggles)
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            String.format("X-Rultor-Revision: %s", TkApp.REV),
            "Vary: Cookie"
        );
    }

    /**
     * Regex takes.
     * @param talks Talks
     * @param pulse Pulse
     * @param toggles Toggles
     * @return Takes
     */
    private static Take regex(final Talks talks,
        final Pulse pulse, final Toggles toggles) {
        return new TkFork(
            new FkRegex("/robots.txt", ""),
            new FkRegex("/ticks", new TkTicks(pulse)),
            new FkRegex("/status", new TkStatus(pulse)),
            new FkRegex("/s/.*", new TkRedirect()),
            new FkRegex("/sitemap", new TkSitemap(talks)),
            new FkRegex(
                "/xsl/.*",
                new TkWithType(new TkClasspath(), "text/xsl")
            ),
            new FkRegex(
                "/js/.*",
                new TkWithType(new TkClasspath(), "text/javascript")
            ),
            new FkRegex(
                "/css/.*",
                new TkWithType(new TkClasspath(), "text/css")
            ),
            new FkRegex("/", new TkHome(talks, toggles)),
            new FkRegex("/b/([/a-zA-Z0-9_\\-\\.]+)", new TkButton()),
            new FkRegex("/t/([0-9]+)-([a-f0-9]+)", new TkDaemon(talks)),
            new FkRegex("/p/([/a-zA-Z0-9_\\-\\.]+)", new TkSiblings(talks)),
            new FkAdminOnly(
                new TkFork(
                    new FkRegex("/t/([0-9]+)", new TkTalk(talks)),
                    new FkRegex("/t/([0-9]+)/kill", new TkTalkKill(talks)),
                    new FkRegex("/t/([0-9]+)/delete", new TkTalkDelete(talks)),
                    new FkRegex("/toggles/read-only", new TkToggles(toggles))
                )
            )
        );
    }

}
