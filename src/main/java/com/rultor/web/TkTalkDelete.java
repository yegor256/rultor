/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
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

/**
 * Front page of a talk.
 *
 * @since 1.50
 */
final class TkTalkDelete implements TkRegex {

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Ctor.
     * @param tks Talks
     */
    TkTalkDelete(final Talks tks) {
        this.talks = tks;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final long number = Long.parseLong(req.matcher().group(1));
        if (!this.talks.exists(number)) {
            throw new RsForward(
                new RsFlash(
                    "there is no such page here",
                    Level.WARNING
                )
            );
        }
        final Talk talk = this.talks.get(number);
        this.talks.delete(talk.name());
        return new RsForward(
            new RsFlash(String.format("talk #%d deleted", number))
        );
    }

}
