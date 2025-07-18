/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.log.Logger;
import com.rultor.Env;
import io.sentry.Sentry;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.fallback.FbChain;
import org.takes.facets.fallback.FbStatus;
import org.takes.facets.fallback.RqFallback;
import org.takes.facets.fallback.TkFallback;
import org.takes.misc.Opt;
import org.takes.rs.RsText;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
import org.takes.tk.TkWrap;

/**
 * App with fallback.
 *
 * @since 1.53
 */
final class TkAppFallback extends TkWrap {

    /**
     * Revision of rultor.
     */
    private static final String REV = Env.read("Rultor-Revision");

    /**
     * Ctor.
     * @param take Take
     */
    TkAppFallback(final Take take) {
        super(TkAppFallback.make(take));
    }

    /**
     * Authenticated.
     * @param take Takes
     * @return Authenticated takes
     */
    private static Take make(final Take take) {
        return new TkFallback(
            take,
            new FbChain(
                new FbStatus(
                    HttpURLConnection.HTTP_NOT_FOUND,
                    new RsWithStatus(
                        new RsText("page not found"),
                        HttpURLConnection.HTTP_NOT_FOUND
                    )
                ),
                req -> {
                    Sentry.captureException(req.throwable());
                    return new Opt.Empty<>();
                },
                req -> new Opt.Single<>(TkAppFallback.fatal(req))
            )
        );
    }

    /**
     * Make fatal error page.
     * @param req Request
     * @return Response
     * @throws IOException If fails
     */
    private static Response fatal(final RqFallback req) throws IOException {
        return new RsWithStatus(
            new RsWithType(
                new RsVelocity(
                    Objects.requireNonNull(TkAppFallback.class.getResource("error.html.vm")),
                    new RsVelocity.Pair(
                        "err",
                        Logger.format("%[exception]s", req.throwable())
                    ),
                    new RsVelocity.Pair("rev", TkAppFallback.REV)
                ),
                "text/html"
            ),
            HttpURLConnection.HTTP_INTERNAL_ERROR
        );
    }

}
