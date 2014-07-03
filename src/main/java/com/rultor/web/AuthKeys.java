/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rexsl.page.auth.HttpBasic;
import com.rexsl.page.auth.Identity;
import java.net.URI;
import javax.validation.constraints.NotNull;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * HTTP Basic Authentication keys.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.DEBUG)
public final class AuthKeys implements HttpBasic.Vault {

    /**
     * Encryption key.
     */
    private final transient String key = Manifests.read("Rultor-SecurityKey");

    @Override
    public Identity authenticate(@NotNull(message = "user can't be NULL")
        final String user, @NotNull(message = "password can't be NULL")
        final String password) {
        Identity identity = new Identity.Simple(
            URN.create(user), "", URI.create("#")
        );
        if (!this.make(identity).equals(password)) {
            identity = Identity.ANONYMOUS;
        }
        return identity;
    }

    /**
     * Make authentication key/password.
     * @param identity Identity to make key for
     * @return Key
     */
    public String make(@NotNull(message = "identity can't be NULL")
        final Identity identity) {
        return DigestUtils.md5Hex(
            String.format("%s-%s", identity.urn(), this.key)
        );
    }

}
