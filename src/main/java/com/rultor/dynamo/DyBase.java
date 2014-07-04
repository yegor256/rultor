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
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rultor.agents.Agent;
import com.rultor.agents.Agents;
import com.rultor.spi.Base;
import com.rultor.spi.Repo;
import com.rultor.spi.User;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base in Dynamo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class DyBase implements Base {

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Public ctor.
     */
    public DyBase() {
        final String key = Manifests.read("Rultor-DynamoKey");
        Credentials creds = new Credentials.Simple(
            key,
            Manifests.read("Rultor-DynamoSecret")
        );
        if ("AAAAABBBBBAAAAABBBBB".equals(key)) {
            creds = new Credentials.Direct(
                creds, Integer.parseInt(System.getProperty("dynamo.port"))
            );
        }
        this.region = new Region.Prefixed(
            new ReRegion(new Region.Simple(creds)),
            Manifests.read("Rultor-DynamoPrefix")
        );
    }

    @Override
    public User user(final URN urn) {
        return new DyUser(this.region, urn);
    }

    @Override
    public void execute() throws IOException {
        for (final Repo repo : this.repos()) {
            this.execute(repo);
        }
    }

    /**
     * Execute one repo.
     * @param repo The repo
     */
    private void execute(final Repo repo) throws IOException {
        final Iterable<Agent> agents = new Agents().make(repo, "");
        for (final Agent agent : agents) {
            agent.execute(repo);
        }
    }

    /**
     * Get all repos.
     * @return Repos
     */
    private Iterable<Repo> repos() {
        return Iterables.transform(
            this.region.table(DyRepos.TBL).frame(),
            new Function<Item, Repo>() {
                @Override
                public Repo apply(final Item input) {
                    return new DyRepo(DyBase.this.region, input);
                }
            }
        );
    }

}
