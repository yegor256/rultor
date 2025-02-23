/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.xml.XML;
import com.rultor.profiles.Profiles;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.Collection;
import org.cactoos.scalar.Or;
import org.takes.Request;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqWrap;

/**
 * Web user.
 *
 * @since 1.50
 */
final class RqUser extends RqWrap {

    /**
     * Ctor.
     * @param req Request
     */
    RqUser(final Request req) {
        super(req);
    }

    @Override
    public String toString() {
        String str = "ANONYMOUS";
        try {
            if (!this.anonymous()) {
                str = this.identity().urn();
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        return str;
    }

    /**
     * Is it an anonymous user?
     * @return TRUE if I'm anonymous
     * @throws IOException If fails
     */
    public boolean anonymous() throws IOException {
        return this.identity().equals(Identity.ANONYMOUS);
    }

    /**
     * Can I see this talk?
     * @param talk The talk
     * @return TRUE if I can see it
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public boolean canSee(final Talk talk) throws IOException {
        final XML xml;
        try {
            xml = new Profiles().fetch(talk).read();
        } catch (final Profile.ConfigException ex) {
            throw new RsForward(new RsFlash(ex), "/");
        }
        final boolean granted;
        final Collection<String> readers = xml.xpath(
            "/p/entry[@key='readers']/item/text()"
        );
        if (readers.isEmpty()) {
            granted = true;
        } else {
            final Identity identity = this.identity();
            try {
                granted = new Or(
                    r -> !identity.equals(Identity.ANONYMOUS)
                        && r.trim().equals(identity.urn()),
                    readers
                ).value();
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Throwable ex) {
                throw new IOException(ex);
            }
        }
        return granted;
    }

    /**
     * Get identity.
     * @return Identity
     * @throws IOException If fails
     */
    private Identity identity() throws IOException {
        return new RqAuth(this).identity();
    }

}
