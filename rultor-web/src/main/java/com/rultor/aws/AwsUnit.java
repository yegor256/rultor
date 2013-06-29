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
package com.rultor.aws;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.jcabi.urn.URN;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Single unit in Dynamo DB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "region", "owner", "name" })
@Loggable(Loggable.DEBUG)
final class AwsUnit implements Unit {

    /**
     * Dynamo DB table name.
     */
    public static final String TABLE = "units";

    /**
     * Dynamo DB table column.
     */
    public static final String KEY_OWNER = "owner";

    /**
     * Dynamo DB table column.
     */
    public static final String KEY_NAME = "name";

    /**
     * Dynamo DB table column.
     */
    private static final String FIELD_SPEC = "spec";

    /**
     * Dynamo DB table column.
     */
    private static final String FIELD_DRAIN = "drain";

    /**
     * Dynamo DB region.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN owner;

    /**
     * Name of the unit.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param urn URN of the user/owner
     * @param label Name of it
     */
    protected AwsUnit(final Region reg, final URN urn, final String label) {
        this.region = reg;
        this.owner = urn;
        this.name = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushBefore
    public void update(@NotNull final Spec spec, @NotNull final Spec drain) {
        this.item().put(
            new Attributes()
                .with(
                    AwsUnit.FIELD_SPEC,
                    new AttributeValue(spec.asText())
            )
                .with(
                    AwsUnit.FIELD_DRAIN,
                    new AttributeValue(drain.asText())
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Cacheable
    public Spec spec() {
        Spec spec;
        final AttributeValue val = this.item().get(AwsUnit.FIELD_SPEC);
        if (val == null) {
            spec = new Spec.Simple();
        } else {
            spec = new Spec.Simple(val.getS());
        }
        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public Spec drain() {
        Spec spec;
        final AttributeValue val = this.item().get(AwsUnit.FIELD_DRAIN);
        if (val == null) {
            spec = new Spec.Simple("com.rultor.drain.Trash()");
        } else {
            spec = new Spec.Simple(val.getS());
        }
        return spec;
    }

    /**
     * Fetch dynamo item.
     * @return The item
     */
    private Item item() {
        final Table table = this.region.table(AwsUnit.TABLE);
        final Iterator<Item> items = table.frame()
            .where(AwsUnit.KEY_OWNER, Conditions.equalTo(this.owner))
            .where(AwsUnit.KEY_NAME, Conditions.equalTo(this.name))
            .iterator();
        Item item;
        if (items.hasNext()) {
            item = items.next();
        } else {
            item = table.put(
                new Attributes()
                    .with(AwsUnit.KEY_OWNER, this.owner)
                    .with(AwsUnit.KEY_NAME, this.name)
            );
        }
        return item;
    }

}
