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
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Map merged from two source maps with priority of second one.
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "map")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class MergedMaps<K, V> implements Map<K, V> {

    /**
     * Underlying map.
     */
    private final transient ArrayMap<K, V> map;

    /**
     * Public ctor.
     * @param first First map to merge with.
     * @param second First map to merge with.
     */
    public MergedMaps(
        @NotNull(message = "map can't be NULL") final Map<K, V> first,
        @NotNull(message = "map can't be NULL") final Map<K, V> second) {
        final ConcurrentHashMap<K, V> collector =
            new ConcurrentHashMap<K, V>(first);
        collector.putAll(second);
        this.map = new ArrayMap<K, V>(collector);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(final K key, final V value) {
        return this.map.put(key, value);
    }

    @Override
    public V remove(final Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> origin) {
        this.map.putAll(origin);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }
}
