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
package com.rultor.users;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.spi.Stand;
import com.rultor.spi.Stands;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Stands in DynamoDB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "region", "owner" })
@Loggable(Loggable.DEBUG)
final class AwsStands implements Stands {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN owner;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param urn URN of the user
     */
    protected AwsStands(final Region reg, final URN urn) {
        this.region = reg;
        this.owner = urn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Stand> iterator() {
        final Iterator<Item> items = this.fetch().iterator();
        return new Iterator<Stand>() {
            @Override
            public boolean hasNext() {
                return items.hasNext();
            }
            @Override
            public Stand next() {
                return new AwsStand(items.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushAfter
    public void create(
        @NotNull(message = "stand name is mandatory when creating")
        @Pattern(
            regexp = "[a-z][-a-z0-9]{2,}",
            message = "Only numbers, letters, and dashes are allowed"
        )
        final String stand) {
        this.region.table(AwsStand.TABLE).put(
            new Attributes()
                .with(AwsStand.HASH_OWNER, this.owner.toString())
                .with(AwsStand.RANGE_STAND, stand)
                .with(AwsStand.FIELD_ACL, "com.rultor.acl.Prohibited()")
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    public boolean contains(
        @NotNull @Pattern(regexp = ".+") final String name) {
        return !this.region.table(AwsStand.TABLE).frame()
            .where(AwsStand.HASH_OWNER, this.owner.toString())
            .where(AwsStand.RANGE_STAND, name)
            .isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stand get(@NotNull @Pattern(regexp = ".+")final String name) {
        final Collection<Item> items = this.region.table(AwsStand.TABLE)
            .frame()
            .where(AwsStand.HASH_OWNER, this.owner.toString())
            .where(AwsStand.RANGE_STAND, name)
            .through(new QueryValve());
        if (items.isEmpty()) {
            throw new NoSuchElementException(
                String.format("Stand `%s` doesn't exist", name)
            );
        }
        return new AwsStand(items.iterator().next());
    }

    /**
     * Fetch all items.
     * @return All stands
     */
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    private Collection<Item> fetch() {
        return this.region.table(AwsStand.TABLE)
            .frame()
            .where(AwsStand.HASH_OWNER, this.owner.toString())
            .through(new QueryValve().withAttributeToGet(AwsStand.RANGE_STAND));
    }

}
