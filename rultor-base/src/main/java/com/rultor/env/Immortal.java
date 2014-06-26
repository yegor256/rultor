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
package com.rultor.env;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.snapshot.Step;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Immortal environments (can't be closed).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class Immortal implements Environments {

    /**
     * Origin environments.
     */
    private final transient Environments origin;

    /**
     * Public ctor.
     * @param envs Original envs
     */
    public Immortal(@NotNull(message = "envs can't be NULL")
        final Environments envs) {
        this.origin = envs;
    }

    @Override
    public Environment acquire() throws IOException {
        return new Immortal.Env(this.origin.acquire());
    }

    @Override
    public Iterator<Environment> iterator() {
        final Iterator<Environment> envs = this.origin.iterator();
        return new Iterator<Environment>() {
            @Override
            public boolean hasNext() {
                return envs.hasNext();
            }
            @Override
            public Environment next() {
                return new Immortal.Env(envs.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Environment without closing feature.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    @Loggable(Loggable.DEBUG)
    private static final class Env implements Environment {
        /**
         * Origin environment.
         */
        private final transient Environment origin;
        /**
         * Ctor.
         * @param env Origin
         */
        protected Env(final Environment env) {
            this.origin = env;
        }
        @Override
        public InetAddress address() throws IOException {
            return this.origin.address();
        }
        @Override
        @Step("immortal environment is not closed")
        public void close() throws IOException {
            assert this.origin != null;
        }
        @Override
        public Map<String, String> badges() {
            return this.origin.badges();
        }
        @Override
        public void badge(final String name, final String value) {
            this.origin.badge(name, value);
        }
    }

}
