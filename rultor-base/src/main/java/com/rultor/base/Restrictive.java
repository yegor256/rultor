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
package com.rultor.base;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Proxy;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Restricts access for certain users.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "work", "origin", "friends" })
@Loggable(Loggable.DEBUG)
public final class Restrictive implements Proxy<Object> {

    /**
     * Coordinates we're in.
     */
    private final transient Coordinates work;

    /**
     * Origin.
     */
    private final transient Object origin;

    /**
     * List of friends (masks).
     */
    private final transient Array<String> friends;

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param frnds List of friends
     * @param instance Original instance
     * @checkstyle ParameterNumber (10 lines)
     */
    public Restrictive(
        @NotNull(message = "work can't be NULL") final Coordinates wrk,
        @NotNull(message = "friends can't be NULL")
        final Collection<String> frnds,
        @NotNull(message = "instance can't be NULL") final Object instance) {
        this.work = wrk;
        this.origin = instance;
        this.friends = new Array<String>(frnds);
    }

    @Override
    public Object object() {
        if (!this.allowed()) {
            throw new SecurityException(
                String.format(
                    "You (%s) are not allowed to touch %s",
                    this.work.owner(),
                    this.origin
                )
            );
        }
        return this.origin;
    }

    /**
     * Access allowed?
     * @return TRUE if allowed
     */
    private boolean allowed() {
        boolean allowed = false;
        for (final String friend : this.friends) {
            if (this.work.owner().matches(friend)
                || this.work.owner().toString().equals(friend)) {
                allowed = true;
                break;
            }
        }
        return allowed;
    }

}
