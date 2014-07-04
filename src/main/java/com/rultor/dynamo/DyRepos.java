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
package com.rultor.dynamo;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.github.Coordinates;
import com.jcabi.urn.URN;
import com.rultor.spi.Repo;
import com.rultor.spi.Repos;
import java.io.IOException;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Repos in Dynamo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class DyRepos implements Repos {

    /**
     * Table name.
     */
    public static final String TBL = "repos";

    /**
     * Bout attribute.
     */
    public static final String HASH = "urn";

    /**
     * Message attribute.
     */
    public static final String RANGE = "id";

    /**
     * Coordinates.
     */
    public static final String ATTR_COORDS = "coordinates";

    /**
     * State of the repo.
     */
    public static final String ATTR_STATE = "state";

    /**
     * Region to work with.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN name;

    /**
     * Ctor.
     * @param reg Region
     * @param urn Name of the user (URN)
     */
    DyRepos(final Region reg, final URN urn) {
        this.region = reg;
        this.name = urn;
    }

    @Override
    public Repo get(final long number) {
        final Iterator<Item> items = this.region.table(DyRepos.TBL)
            .frame()
            .through(new QueryValve())
            .where(DyRepos.HASH, Conditions.equalTo(this.name))
            .where(DyRepos.RANGE, Conditions.equalTo(number))
            .iterator();
        if (!items.hasNext()) {
            throw new IllegalArgumentException(
                String.format("repository #%d not found", number)
            );
        }
        return new DyRepo(this.region, items.next());
    }

    @Override
    public Iterable<Repo> iterate() {
        return Iterables.transform(
            this.region.table(DyRepos.TBL)
                .frame()
                .through(new QueryValve())
                .where(DyRepos.HASH, Conditions.equalTo(this.name)),
            new Function<Item, Repo>() {
                @Override
                public Repo apply(final Item input) {
                    return new DyRepo(DyRepos.this.region, input);
                }
            }
        );
    }

    @Override
    public long add(final Coordinates coords) throws IOException {
        final long number = System.currentTimeMillis();
        this.region.table(DyRepos.TBL).put(
            new Attributes()
                .with(DyRepos.HASH, this.name)
                .with(DyRepos.RANGE, number)
                .with(DyRepos.ATTR_COORDS, coords.toString())
                .with(DyRepos.ATTR_STATE, "{}")
        );
        return number;
    }

    @Override
    public void delete(final long number) {
        Iterators.removeIf(
            this.region.table(DyRepos.TBL)
                .frame()
                .through(new QueryValve())
                .where(DyRepos.HASH, Conditions.equalTo(this.name))
                .where(DyRepos.RANGE, Conditions.equalTo(number))
                .iterator(),
            Predicates.alwaysTrue()
        );
    }
}
