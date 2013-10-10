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
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.urn.URN;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulses;
import com.rultor.spi.Spec;
import com.rultor.spi.Stand;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Single stand in Dynamo DB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "item")
@Loggable(Loggable.DEBUG)
final class AwsStand implements Stand {

    /**
     * Dynamo DB table name.
     */
    public static final String TABLE = "stands";

    /**
     * Dynamo DB table column.
     */
    public static final String HASH_OWNER = "owner";

    /**
     * Dynamo DB table column.
     */
    public static final String RANGE_STAND = "stand";

    /**
     * Dynamo DB table column.
     */
    public static final String FIELD_ACL = "acl";

    /**
     * Dynamo DB table column.
     */
    public static final String FIELD_WIDGETS = "widgets";

    /**
     * Item.
     */
    private final transient Item item;

    /**
     * Public ctor.
     * @param itm Item from Dynamo
     */
    protected AwsStand(final Item itm) {
        this.item = itm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushAfter
    @RetryOnFailure(verbose = false)
    public void update(
        @NotNull(message = "ACL spec can't be NULL") final Spec spec,
        @NotNull(message = "widgets spec can't be NULL") final Spec widgets) {
        this.item.put(
            new Attributes()
                .with(AwsStand.FIELD_ACL, spec.asText())
                .with(AwsStand.FIELD_WIDGETS, widgets.asText())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "ACL of a stand is never NULL")
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    @RetryOnFailure(verbose = false)
    public Spec acl() {
        Spec spec;
        if (this.item.has(AwsStand.FIELD_ACL)) {
            spec = new Spec.Simple(this.item.get(AwsStand.FIELD_ACL).getS());
        } else {
            spec = new Spec.Simple("com.rultor.acl.Prohibited()");
        }
        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "widgets of a stand is never NULL")
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    @RetryOnFailure(verbose = false)
    public Spec widgets() {
        Spec spec;
        if (this.item.has(AwsStand.FIELD_WIDGETS)) {
            spec = new Spec.Simple(
                this.item.get(AwsStand.FIELD_WIDGETS).getS()
            );
        } else {
            spec = new Spec.Simple("[]");
        }
        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    @RetryOnFailure(verbose = false)
    public String name() {
        return this.item.get(AwsStand.RANGE_STAND).getS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    @RetryOnFailure(verbose = false)
    public URN owner() {
        return URN.create(this.item.get(AwsStand.HASH_OWNER).getS());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pulses pulses() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void post(final Coordinates pulse, final long nano,
        final String xembly) {
        throw new UnsupportedOperationException();
    }

}
