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
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Talks in Dynamo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class DyTalks implements Talks {

    /**
     * Table name.
     */
    public static final String TBL = "talks";

    /**
     * Repository number.
     */
    public static final String HASH = "repo";

    /**
     * Talk ID.
     */
    public static final String RANGE = "name";

    /**
     * XML of the talk.
     */
    public static final String ATTR_XML = "xml";

    /**
     * Region to work with.
     */
    private final transient Region region;

    /**
     * Repo number.
     */
    private final transient long repo;

    /**
     * Ctor.
     * @param reg Region
     * @param rpo Repository number
     */
    DyTalks(final Region reg, final long rpo) {
        this.region = reg;
        this.repo = rpo;
    }

    @Override
    public boolean exists(final String name) {
        return this.region.table(DyTalks.TBL)
            .frame()
            .through(new QueryValve())
            .where(DyTalks.HASH, Conditions.equalTo(this.repo))
            .where(DyTalks.RANGE, name)
            .iterator().hasNext();
    }

    @Override
    public Talk get(final String name) {
        return new DyTalk(
            this.region.table(DyTalks.TBL)
                .frame()
                .through(new QueryValve())
                .where(DyTalks.HASH, Conditions.equalTo(this.repo))
                .where(DyTalks.RANGE, name)
                .iterator().next()
        );
    }

    @Override
    public void create(final String name) throws IOException {
        this.region.table(DyTalks.TBL).put(
            new Attributes()
                .with(DyTalks.HASH, this.repo)
                .with(DyTalks.RANGE, name)
                .with(DyTalks.ATTR_XML, "<talks/>")
        );
    }

    @Override
    public Iterable<Talk> iterate() {
        return Iterables.transform(
            this.region.table(DyTalks.TBL)
                .frame()
                .through(new QueryValve())
                .where(DyTalks.HASH, Conditions.equalTo(this.repo)),
            new Function<Item, Talk>() {
                @Override
                public Talk apply(final Item input) {
                    return new DyTalk(input);
                }
            }
        );
    }
}
