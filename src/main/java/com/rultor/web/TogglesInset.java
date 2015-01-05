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

import com.rexsl.page.BasePage;
import com.rexsl.page.Inset;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.auth.Identity;
import com.rultor.Toggles;
import javax.ws.rs.core.Response;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
final class TogglesInset implements Inset {

    /**
     * BaseRs.
     */
    private final transient BaseRs base;

    /**
     * Ctor.
     * @param res Base
     */
    TogglesInset(final BaseRs res) {
        this.base = res;
    }

    @Override
    public void render(final BasePage<?, ?> page,
        final Response.ResponseBuilder builder) {
        final Toggles toggles = new Toggles();
        final JaxbBundle bundle = new JaxbBundle("toggles");
        bundle.add("read-only", Boolean.toString(toggles.readOnly())).up();
        if (!this.base.auth().identity().equals(Identity.ANONYMOUS)) {
            bundle.link(
                new Link(
                    "sw:read-only",
                    this.base.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(TogglesRs.class)
                        .path(TogglesRs.class, "readOnly")
                        .build()
                )
            );
        }
        page.append(bundle);
    }

}
