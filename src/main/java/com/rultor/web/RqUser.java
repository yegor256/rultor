/**
 * Copyright (c) 2009-2018, rultor.com
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
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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
