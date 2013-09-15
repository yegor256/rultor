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
import com.jcabi.immutable.ArrayMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Map that never discloses its content.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class SecretMap implements Map<String, Object> {

    /**
     * Origin.
     */
    private final transient ArrayMap<String, Object> origin;

    /**
     * Public ctor.
     * @param map Original map
     */
    public SecretMap(
        @NotNull(message = "map can't be NULL") final Map<String, Object> map) {
        this.origin = new ArrayMap<String, Object>(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.origin.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.origin.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        return this.origin.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(final Object value) {
        return this.origin.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final Object key) {
        return this.origin.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(final String key, final Object value) {
        return this.origin.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(final Object key) {
        return this.origin.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(final Map<? extends String, ? extends Object> map) {
        this.origin.putAll(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.origin.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> keySet() {
        return this.origin.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> values() {
        return this.origin.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.origin.entrySet();
    }

}
