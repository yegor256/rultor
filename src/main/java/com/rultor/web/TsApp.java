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
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.takes.Request;
import org.takes.Take;
import org.takes.Takes;
import org.takes.facets.auth.PsByFlag;
import org.takes.facets.auth.PsChain;
import org.takes.facets.auth.PsCookie;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.PsLogout;
import org.takes.facets.auth.TsAuth;
import org.takes.facets.auth.codecs.CcCompact;
import org.takes.facets.auth.codecs.CcHex;
import org.takes.facets.auth.codecs.CcSafe;
import org.takes.facets.auth.codecs.CcSalted;
import org.takes.facets.auth.codecs.CcXOR;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.fallback.Fallback;
import org.takes.facets.fallback.RqFallback;
import org.takes.facets.fallback.TsFallback;
import org.takes.facets.flash.TsFlash;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.Target;
import org.takes.facets.fork.TsFork;
import org.takes.facets.forward.TsForward;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
import org.takes.tk.TkFixed;
import org.takes.tk.TkRedirect;
import org.takes.ts.TsClasspath;
import org.takes.ts.TsGzip;
import org.takes.ts.TsMeasured;
import org.takes.ts.TsVersioned;
import org.takes.ts.TsWithHeaders;
import org.takes.ts.TsWithType;
import org.takes.ts.TsWrap;

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
public final class TsApp extends TsWrap {

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
    public TsApp(final Talks talks, final Pulse pulse,
        final Toggles toggles) {
        super(TsApp.make(talks, pulse, toggles));
    }

    /**
     * Ctor.
     * @param talks Talks
     * @param pulse Pulse
     * @param toggles Toggles
     * @return Takes
     */
    private static Takes make(final Talks talks,
        final Pulse pulse, final Toggles toggles) {
        if (!"UTF-8".equals(Charset.defaultCharset().name())) {
            throw new IllegalStateException(
                String.format(
                    "default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        final Takes takes = new TsGzip(
            TsApp.fallback(
                new TsFlash(
                    TsApp.auth(
                        new TsForward(TsApp.regex(talks, pulse, toggles))
                    )
                )
            )
        );
        return new TsWithHeaders(
            new TsVersioned(new TsMeasured(takes)),
            String.format("X-Rultor-Revision: %s", TsApp.REV),
            "Vary: Cookie"
        );
    }

    /**
     * Authenticated.
     * @param takes Takes
     * @return Authenticated takes
     */
    private static Takes fallback(final Takes takes) {
        return new TsFallback(
            takes,
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Fallback() {
                @Override
                public Take take(final RqFallback req) throws IOException {
                    final String err = ExceptionUtils.getStackTrace(
                        req.throwable()
                    );
                    return new TkFixed(
                        new RsWithStatus(
                            new RsWithType(
                                new RsVelocity(
                                    this.getClass().getResource(
                                        "error.html.vm"
                                    ),
                                    new RsVelocity.Pair("err", err),
                                    new RsVelocity.Pair("rev", TsApp.REV)
                                ),
                                "text/html"
                            ),
                            HttpURLConnection.HTTP_INTERNAL_ERROR
                        )
                    );
                }
            }
        );
    }

    /**
     * Authenticated.
     * @param takes Takes
     * @return Authenticated takes
     */
    private static Takes auth(final Takes takes) {
        return new TsAuth(
            takes,
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
    private static Takes regex(final Talks talks,
        final Pulse pulse, final Toggles toggles) {
        return new TsFork(
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
                new TsWithType(new TsClasspath(), "text/xsl")
            ),
            new FkRegex(
                "/js/.*",
                new TsWithType(new TsClasspath(), "text/javascript")
            ),
            new FkRegex(
                "/css/.*",
                new TsWithType(new TsClasspath(), "text/css")
            ),
            new FkRegex(
                "/toggles/read-only",
                new Takes() {
                    @Override
                    public Take route(final Request req) throws IOException {
                        return new TkAdminOnly(new TkToggles(toggles), req);
                    }
                }
            ),
            new FkRegex(
                "/",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) {
                        return new TkHome(talks, toggles, req);
                    }
                }
            ),
            new FkRegex(
                "/b/([/a-zA-Z0-9_\\-\\.]+)",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) {
                        return new TkButton(req.matcher().group(1));
                    }
                }
            ),
            new FkRegex(
                "/t/([0-9]+)-([a-f0-9]+)",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) throws IOException {
                        return new TkDaemon(
                            req, talks,
                            Long.parseLong(req.matcher().group(1)),
                            req.matcher().group(2)
                        );
                    }
                }
            ),
            new FkRegex(
                "/p/([/a-zA-Z0-9_\\-\\.]+)",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) throws IOException {
                        return new TkSiblings(
                            talks, req.matcher().group(1), req
                        );
                    }
                }
            ),
            new FkRegex(
                "/t/([0-9]+)",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) throws IOException {
                        return new TkAdminOnly(
                            new TkTalk(
                                talks, req,
                                Long.parseLong(req.matcher().group(1))
                            ),
                            req
                        );
                    }
                }
            ),
            new FkRegex(
                "/t/([0-9]+)/kill",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) throws IOException {
                        return new TkAdminOnly(
                            new TkTalkKill(
                                talks, Long.parseLong(req.matcher().group(1))
                            ),
                            req
                        );
                    }
                }
            ),
            new FkRegex(
                "/t/([0-9]+)/delete",
                new Target<RqRegex>() {
                    @Override
                    public Take route(final RqRegex req) throws IOException {
                        return new TkAdminOnly(
                            new TkTalkDelete(
                                talks, Long.parseLong(req.matcher().group(1))
                            ),
                            req
                        );
                    }
                }
            )
        );
    }

}
