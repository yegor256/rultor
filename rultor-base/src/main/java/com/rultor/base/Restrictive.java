/**
 * Copyright (c) 2009-2013, rultor.com
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
import com.rultor.spi.Proxy;
import com.rultor.spi.Work;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Restricts access for certain users.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
public final class Restrictive implements Proxy {

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Origin.
     */
    private final transient Object origin;

    /**
     * List of friends (masks).
     */
    private final transient String[] friends;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param frnds List of friends
     * @param instance Original instance
     * @checkstyle ParameterNumber (5 lines)
     */
    public Restrictive(@NotNull final Work wrk, final Collection<String> frnds,
        @NotNull final Object instance) {
        this.work = wrk;
        this.origin = instance;
        this.friends = frnds.toArray(new String[frnds.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object object() {
        if (!this.allowed()) {
            throw new SecurityException(
                String.format(
                    "You (%s) are not allowed to touch me",
                    this.work.owner()
                )
            );
        }
        return this.origin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (!this.allowed()) {
            throw new SecurityException(
                String.format(
                    "You (%s) are not allowed to use '%s'",
                    this.work.owner(),
                    this.origin
                )
            );
        }
        return this.origin.toString();
    }

    /**
     * Access allowed?
     * @return TRUE if allowed
     */
    private boolean allowed() {
        boolean allowed = false;
        for (String friend : this.friends) {
            if (this.work.owner().matches(friend)) {
                allowed = true;
                break;
            }
        }
        return allowed;
    }

}
