/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.rultor.Env;
import java.util.regex.Pattern;
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
import org.takes.facets.auth.codecs.CcXor;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.fork.FkFixed;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.TkFork;
import org.takes.tk.TkRedirect;
import org.takes.tk.TkWrap;

/**
 * Authenticated app.
 *
 * @since 1.53
 */
final class TkAppAuth extends TkWrap {

    /**
     * Ctor.
     * @param take Take
     */
    TkAppAuth(final Take take) {
        super(TkAppAuth.make(take));
    }

    /**
     * Authenticated.
     * @param take Takes
     * @return Authenticated takes
     */
    private static Take make(final Take take) {
        return new TkAuth(
            new TkFork(
                new FkParams(
                    PsByFlag.class.getSimpleName(),
                    Pattern.compile(".+"),
                    new TkRedirect()
                ),
                new FkFixed(take)
            ),
            new PsChain(
                new PsFake(
                    Env.read("Rultor-DynamoKey").startsWith("AAAA")
                ),
                new PsByFlag(
                    new PsByFlag.Pair(
                        PsGithub.class.getSimpleName(),
                        new PsGithub(
                            Env.read("Rultor-GithubId"),
                            Env.read("Rultor-GithubSecret")
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
                            new CcXor(
                                new CcSalted(new CcCompact()),
                                Env.read("Rultor-SecurityKey")
                            )
                        )
                    )
                )
            )
        );
    }

}
