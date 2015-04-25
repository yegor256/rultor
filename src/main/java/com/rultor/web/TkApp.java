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
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.PsByFlag;
import org.takes.facets.auth.PsChain;
import org.takes.facets.auth.PsCookie;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.PsLogout;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.auth.codecs.CcCompact;
import org.takes.facets.auth.codecs.CcHex;
import org.takes.facets.auth.codecs.CcSafe;
import org.takes.facets.auth.codecs.CcSalted;
import org.takes.facets.auth.codecs.CcXOR;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.fallback.Fallback;
import org.takes.facets.fallback.RqFallback;
import org.takes.facets.fallback.TkFallback;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.50
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings({
    "PMD.TooManyMethods", "PMD.ExcessiveMethodLength",
    "PMD.ExcessiveImports"
})
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
                    "default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        final Take takes = new TkGzip(
            TkApp.fallback(
                new TkFlash(
                    TkApp.auth(
                        new TkForward(TkApp.regex(talks, pulse, toggles))
                    )
                )
            )
        );
        return new TkWithHeaders(
            new TkVersioned(new TkMeasured(takes)),
            String.format("X-Rultor-Revision: %s", TkApp.REV),
            "Vary: Cookie"
        );
    }

    /**
     * Authenticated.
     * @param takes Takes
     * @return Authenticated takes
     */
    private static Take fallback(final Take takes) {
        return new TkFallback(
            takes,
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Fallback() {
                @Override
                public Iterator<Response> route(final RqFallback req)
                    throws IOException {
                    final String err = ExceptionUtils.getStackTrace(
                        req.throwable()
                    );
                    return Collections.<Response>singleton(
                        new RsWithStatus(
                            new RsWithType(
                                new RsVelocity(
                                    this.getClass().getResource(
                                        "error.html.vm"
                                    ),
                                    new RsVelocity.Pair("err", err),
                                    new RsVelocity.Pair("rev", TkApp.REV)
                                ),
                                "text/html"
                            ),
                            HttpURLConnection.HTTP_INTERNAL_ERROR
                        )
                    ).iterator();
                }
            }
        );
    }

    /**
     * Authenticated.
     * @param take Takes
     * @return Authenticated takes
     */
    private static Take auth(final Take take) {
        return new TkAuth(
            take,
            new PsChain(
                new PsFake(
                    Manifests.read("Rultor-DynamoKey").startsWith("AAAA")
                ),
                new PsByFlag(
                    new PsByFlag.Pair(
                        PsGithub.class.getSimpleName(),
                        new PsGithub(
                            Manifests.read("Rultor-GithubId"),
                            Manifests.read("Rultor-GithubSecret")
                        )
                    ),
                    new PsByFlag.Pair(
                        PsLogout.class.getSimpleName(),
                        new PsLogout()
                    )
                ),
                new PsCookie(
                    new CcSafe(
                        new CcHex(
                            new CcXOR(
                                new CcSalted(new CcCompact()),
                                Manifests.read("Rultor-SecurityKey")
                            )
                        )
                    )
                )
            )
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
            new FkParams(
                PsByFlag.class.getSimpleName(),
                Pattern.compile(".+"),
                new TkRedirect()
            ),
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
