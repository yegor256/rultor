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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Key in a State.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Key {

    /**
     * Does it exist?
     * @return TRUE if exists
     */
    boolean exists();

    /**
     * Get value (runtime exception if absent, use {@link #exists()}.
     * @return Data
     */
    String value();

    /**
     * Put data.
     * @param value Value
     */
    void put(String value);

    /**
     * Get or default.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "origin", "def" })
    final class Default implements Key {
        /**
         * Encapsulated key.
         */
        private final transient Key origin;
        /**
         * Default.
         */
        private final transient String def;
        /**
         * Ctor.
         * @param key Key
         * @param dflt Default
         */
        public Default(final Key key, final String dflt) {
            this.origin = key;
            this.def = dflt;
        }
        @Override
        public boolean exists() {
            return this.origin.exists();
        }
        @Override
        public String value() {
            final String value;
            if (this.origin.exists()) {
                value = this.origin.value();
            } else {
                value = this.def;
                this.put(value);
            }
            return value;
        }
        @Override
        public void put(final String value) {
            this.origin.put(value);
        }
    }

}
