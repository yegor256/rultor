/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqHref;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeaders;
import org.takes.rs.RsWithType;

/**
 * Button.
 *
 * @since 1.50
 */
final class TkButton implements TkRegex {

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Iterator<String> size = new RqHref.Base(req)
            .href().param("size").iterator();
        String suffix = "m";
        if (size.hasNext()) {
            suffix = size.next().toLowerCase(Locale.ENGLISH);
        }
        return new RsWithType(
            new RsWithHeaders(
                new RsWithBody(
                    Objects.requireNonNull(
                        this.getClass().getResource(
                            String.format("button-%s.svg", suffix)
                        )
                    )
                ),
                "Cache-Control: no-cache"
            ),
            "image/svg+xml"
        );
    }
}
