/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.rultor.Toggles;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;

/**
 * Toggles.
 *
 * @since 1.0
 */
final class TkToggles implements Take {

    /**
     * Toggles.
     */
    private final transient Toggles toggles;

    /**
     * Ctor.
     * @param tgls Toggles
     */
    TkToggles(final Toggles tgls) {
        this.toggles = tgls;
    }

    @Override
    public Response act(final Request req) throws IOException {
        this.toggles.toggle();
        return new RsForward(
            new RsFlash(
                String.format(
                    "read-only mode set to %B", this.toggles.readOnly()
                )
            )
        );
    }

}
