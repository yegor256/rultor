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
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.rultor.agents.Agents;
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
     * Talk unique name.
     */
    public static final String HASH = "name";

    /**
     * Is it active (1) or archived (0).
     */
    public static final String ATTR_ACTIVE = "active";

    /**
     * XML of the talk.
     */
    public static final String ATTR_XML = "xml";

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Public ctor.
     */
    public DyTalks() {
        final String key = Manifests.read("Rultor-DynamoKey");
        Credentials creds = new Credentials.Simple(
            key,
            Manifests.read("Rultor-DynamoSecret")
        );
        if ("AAAAABBBBBAAAAABBBBB".equals(key)) {
            final int port = Integer.parseInt(
                System.getProperty("dynamo.port")
            );
            creds = new Credentials.Direct(creds, port);
            Logger.warn(Agents.class, "test DynamoDB at port #%d", port);
        }
        this.region = new Region.Prefixed(
            new ReRegion(new Region.Simple(creds)), "rt-"
        );
    }

    @Override
    public boolean exists(final String name) {
        return this.region.table(DyTalks.TBL)
            .frame()
            .through(new QueryValve().withLimit(1))
            .where(DyTalks.HASH, name)
            .iterator().hasNext();
    }

    @Override
    public Talk get(final String name) {
        return new DyTalk(
            this.region.table(DyTalks.TBL)
                .frame()
                .through(new QueryValve().withLimit(1))
                .where(DyTalks.HASH, name)
                .iterator().next()
        );
    }

    @Override
    public void create(final String name) throws IOException {
        this.region.table(DyTalks.TBL).put(
            new Attributes()
                .with(DyTalks.HASH, name)
                .with(DyTalks.ATTR_ACTIVE, Boolean.toString(true))
                .with(DyTalks.ATTR_XML, "<talk/>")
        );
    }

    @Override
    public Iterable<Talk> iterate() {
        return Iterables.transform(
            this.region.table(DyTalks.TBL).frame(),
            new Function<Item, Talk>() {
                @Override
                public Talk apply(final Item input) {
                    return new DyTalk(input);
                }
            }
        );
    }

    @Override
    public Iterable<Talk> active() {
        return Iterables.transform(
            this.region.table(DyTalks.TBL)
                .frame()
                .where(DyTalks.ATTR_ACTIVE, Boolean.toString(true)),
            new Function<Item, Talk>() {
                @Override
                public Talk apply(final Item input) {
                    return new DyTalk(input);
                }
            }
        );
    }
}
