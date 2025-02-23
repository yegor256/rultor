/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.fork.Fork;
import org.takes.facets.forward.RsForward;
import org.takes.misc.Opt;

/**
 * Admin only.
 *
 * @since 1.50
 */
final class FkAdminOnly implements Fork {

    /**
     * Original take.
     */
    private final transient Take origin;

    /**
     * Ctor.
     * @param take Original
     */
    FkAdminOnly(final Take take) {
        this.origin = take;
    }

    @Override
    public Opt<Response> route(final Request req) throws Exception {
        final Identity identity = new RqAuth(req).identity();
        final Opt<Response> opt;
        if (identity.equals(Identity.ANONYMOUS)) {
            opt = new Opt.Single<>(
                new RsForward(
                    new RsFlash(
                        "sorry, you have to be logged in to see this page",
                        Level.WARNING
                    )
                )
            );
        } else if ("urn:github:526301".equals(identity.urn())) {
            opt = new Opt.Single<>(this.origin.act(req));
        } else {
            opt = new Opt.Single<>(
                new RsForward(
                    new RsFlash(
                        "sorry, but this entrance is \"staff only\"",
                        Level.WARNING
                    )
                )
            );
        }
        return opt;
    }
}
