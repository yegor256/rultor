/**
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
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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
