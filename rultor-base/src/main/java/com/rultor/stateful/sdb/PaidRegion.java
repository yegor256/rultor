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
package com.rultor.stateful.sdb;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.simpledb.Domain;
import com.jcabi.simpledb.Item;
import com.jcabi.simpledb.Region;
import com.rultor.spi.Wallet;
import com.rultor.tools.Dollars;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon SimpleDB paid region.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "wallet", "origin" })
@Loggable(Loggable.DEBUG)
public final class PaidRegion implements Region {

    /**
     * Wallet to charge.
     */
    private final transient Wallet wallet;

    /**
     * Original region.
     */
    private final transient Region origin;

    /**
     * Public ctor.
     * @param wlt Wallet
     * @param region Origin region
     */
    public PaidRegion(
        @NotNull(message = "wallet can't be NULL") final Wallet wlt,
        @NotNull(message = "region can't be NULL") final Region region) {
        this.wallet = wlt;
        this.origin = region;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AmazonSimpleDB aws() {
        return this.origin.aws();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Domain domain(final String name) {
        final Domain domain = this.origin.domain(name);
        return new Domain() {
            @Override
            public void create() {
                domain.create();
            }
            @Override
            public void drop() {
                domain.drop();
            }
            @Override
            public String name() {
                return domain.name();
            }
            @Override
            public Item item(final String name) {
                return PaidRegion.this.wrap(domain.item(name));
            }
            @Override
            public Iterable<Item> select(final SelectRequest request) {
                return new Iterable<Item>() {
                    @Override
                    public Iterator<Item> iterator() {
                        final Iterator<Item> items =
                            domain.select(request).iterator();
                        PaidRegion.this.wallet.charge(
                            Logger.format("selected SimpleDB domain %s", name),
                            new Dollars(Tv.FIVE)
                        );
                        return new Iterator<Item>() {
                            @Override
                            public boolean hasNext() {
                                return items.hasNext();
                            }
                            @Override
                            public Item next() {
                                return PaidRegion.this.wrap(items.next());
                            }
                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * Wrap the item.
     * @param item The item to wrap
     * @return Wrapped one
     */
    private Item wrap(final Item item) {
        return new Item() {
            @Override
            public String name() {
                return item.name();
            }
            @Override
            public int size() {
                return item.size();
            }
            @Override
            public boolean isEmpty() {
                return item.isEmpty();
            }
            @Override
            public boolean containsKey(final Object key) {
                return item.containsKey(key);
            }
            @Override
            public boolean containsValue(final Object value) {
                return item.containsValue(value);
            }
            @Override
            public String get(final Object key) {
                return item.get(key);
            }
            @Override
            public String put(final String key, final String value) {
                return item.put(key, value);
            }
            @Override
            public String remove(final Object key) {
                return item.remove(key);
            }
            @Override
            public void putAll(
                final Map<? extends String, ? extends String> map) {
                final long start = System.currentTimeMillis();
                item.putAll(map);
                PaidRegion.this.wallet.charge(
                    Logger.format(
                        "saved %d SimpleDB attribute(s) into %s in %[ms]s",
                        map.size(), item, System.currentTimeMillis() - start
                    ),
                    new Dollars(Tv.FIVE)
                );
            }
            @Override
            public void clear() {
                final long start = System.currentTimeMillis();
                item.clear();
                PaidRegion.this.wallet.charge(
                    Logger.format(
                        "removed SimpleDB item %s %[ms]s",
                        item, System.currentTimeMillis() - start
                    ),
                    new Dollars(Tv.FIVE)
                );
            }
            @Override
            public Set<String> keySet() {
                return item.keySet();
            }
            @Override
            public Collection<String> values() {
                return item.values();
            }
            @Override
            public Set<Map.Entry<String, String>> entrySet() {
                final long start = System.currentTimeMillis();
                final Set<Map.Entry<String, String>> entries = item.entrySet();
                PaidRegion.this.wallet.charge(
                    Logger.format(
                        "loaded %d SimpleDB attribute(s) from %s in %[ms]s",
                        entries.size(), item,
                        System.currentTimeMillis() - start
                    ),
                    new Dollars(Tv.FIVE)
                );
                return entries;
            }
        };
    }

}
