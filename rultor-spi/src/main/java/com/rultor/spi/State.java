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
package com.rultor.spi;

import com.jcabi.aspects.Loggable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Unit-specific mutable state.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
public interface State {

    /**
     * Get value by key (runtime exception if it's absent).
     * @param key The key
     * @return Value
     */
    @NotNull
    String get(@NotNull String key);

    /**
     * Does it have this key?
     * @param key The key
     * @return TRUE if key exists
     */
    boolean has(@NotNull String key);

    /**
     * Set if absent, don't touch if already present.
     * @param key The key
     * @param value The value
     * @return TRUE if it was actually saved
     */
    @NotNull
    boolean checkAndSet(@NotNull String key, @NotNull String value);

    /**
     * In memory state.
     */
    @Loggable(Loggable.INFO)
    @ToString
    @EqualsAndHashCode(of = "map")
    final class Memory implements State {
        /**
         * Map of values.
         */
        private final transient ConcurrentMap<String, String> map =
            new ConcurrentHashMap<String, String>(0);
        /**
         * {@inheritDoc}
         */
        @Override
        public String get(final String key) {
            final String value = this.map.get(key);
            Validate.notNull(value, "key %s is absent in state", key);
            return value;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean has(final String key) {
            return this.map.containsKey(key);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean checkAndSet(final String key, final String value) {
            final String before = this.map.putIfAbsent(key, value);
            return !value.equals(before);
        }
    }

}
