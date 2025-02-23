/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rs.xe.XeDirectives;
import org.xembly.Directives;

/**
 * Front page of a talk.
 *
 * @since 1.50
 */
final class TkTalk implements TkRegex {

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Ctor.
     * @param tks Talks
     */
    TkTalk(final Talks tks) {
        this.talks = tks;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final long number = Long.parseLong(req.matcher().group(1));
        if (!this.talks.exists(number)) {
            throw new RsForward(
                new RsFlash(
                    "There is no such page here",
                    Level.WARNING
                )
            );
        }
        final Talk talk = this.talks.get(number);
        return new RsPage(
            "/xsl/talk.xsl",
            req,
            new XeDirectives(
                new Directives().add("talk")
                    .add("number").set(Long.toString(talk.number())).up()
                    .add("name").set(talk.name()).up()
                    .add("content").set(talk.read().toString())
            )
        );
    }

}
