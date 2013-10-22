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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.simpledb.Domain;
import com.jcabi.simpledb.Item;
import com.jcabi.simpledb.Region;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * SanitizedDomains wraps domains of underlying region with filtered items.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle AnonInnerLength (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "limit", "origin" })
@Loggable(Loggable.DEBUG)
public final class SanitizedDomains implements Region {

    /**
     * Wallet to charge.
     */
    private final transient long limit;

    /**
     * Original region.
     */
    private final transient Region origin;

    /**
     * Public ctor.
     * @param region Origin region
     * @param lmt Limit of item's age
     */
    public SanitizedDomains(
        @NotNull(message = "region can't be NULL") final Region region,
        final long lmt) {
        this.origin = region;
        this.limit = lmt;
    }

    @Override
    public AmazonSimpleDB aws() {
        return this.origin.aws();
    }

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
                return domain.item(name);
            }
            @Override
            public Iterable<Item> select(final SelectRequest request) {
                return new Iterable<Item>() {
                    @Override
                    public Iterator<Item> iterator() {
                        return Iterators.filter(
                            domain.select(request).iterator(),
                            new Predicate<Item>() {
                                @Override
                                public boolean apply(final Item item) {
                                    return Long.parseLong(item.get("time"))
                                        < SanitizedDomains.this.limit;
                                }
                            }
                        );
                    }
                };
            }
        };
    }
}
