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

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Single user in Dynamo DB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "region", "name" })
@Loggable(Loggable.DEBUG)
final class AwsUser implements User {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN name;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param urn URN of the user
     */
    protected AwsUser(final Region reg, final URN urn) {
        this.region = reg;
        this.name = urn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public URN urn() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Map<String, Unit> units() {
        return new AbstractMap<String, Unit>() {
            @Override
            public Set<Map.Entry<String, Unit>> entrySet() {
                return AwsUser.this.fetch().entrySet();
            }
            @Override
            public Unit remove(final Object unit) {
                AwsUser.this.remove(unit.toString());
                return null;
            }
            @Override
            public Unit put(final String txt, final Unit unit) {
                AwsUser.this.create(txt);
                return null;
            }
        };
    }

    /**
     * Create a new unit.
     * @param unt Name of it
     * @return The unit created
     */
    @Cacheable.FlushAfter
    private Unit create(@NotNull @Pattern(
        regexp = "[-a-z0-9]+",
        message = "Only numbers, letters, and dashes are allowed")
        final String unt) {
        if (this.units().containsKey(unt)) {
            throw new IllegalArgumentException(
                String.format("Unit '%s' already exists", unt)
            );
        }
        final Unit unit = this.unit(unt);
        unit.update(
            new Spec.Simple(),
            new Spec.Simple("com.rultor.drain.Trash()")
        );
        return unit;
    }

    /**
     * Remove this unit.
     * @param unit Name of it
     */
    @Cacheable.FlushAfter
    private void remove(@NotNull final String unit) {
        final Iterator<Item> items = this.region.table(AwsUnit.TABLE).frame()
            .where(AwsUnit.KEY_OWNER, Conditions.equalTo(this.name))
            .where(AwsUnit.KEY_NAME, Conditions.equalTo(unit))
            .iterator();
        if (!items.hasNext()) {
            throw new NoSuchElementException(
                String.format("unit '%s' not found", unit)
            );
        }
        items.next();
        items.remove();
    }

    /**
     * Make a unit using the name.
     * @param unit Name of the unit
     * @return The unit
     */
    private Unit unit(final String unit) {
        return new AwsUnit(this.region, this.name, unit);
    }

    /**
     * Fetch all units.
     * @return Map of them
     */
    @Cacheable
    private Map<String, Unit> fetch() {
        final ConcurrentMap<String, Unit> units =
            new ConcurrentSkipListMap<String, Unit>();
        final Collection<Item> items = this.region.table(AwsUnit.TABLE)
            .frame().where(AwsUnit.KEY_OWNER, Conditions.equalTo(this.name));
        for (Item item : items) {
            final String unit = item.get(AwsUnit.KEY_NAME).getS();
            if (!units.containsKey(unit)) {
                units.put(unit, this.unit(unit));
            }
        }
        return Collections.unmodifiableMap(units);
    }

}
