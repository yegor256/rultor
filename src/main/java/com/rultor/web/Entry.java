/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.web;

import co.stateful.RtSttc;
import co.stateful.Sttc;
import co.stateful.cached.CdSttc;
import co.stateful.retry.ReSttc;
import com.google.common.collect.EvictingQueue;
import com.jcabi.aspects.Cacheable;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.wire.RetryWire;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rultor.Toggles;
import com.rultor.dynamo.DyTalks;
import com.rultor.spi.Pulse;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.takes.http.Exit;
import org.takes.http.FtCLI;

/**
 * Command line entry.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.50
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Entry {

    /**
     * Arguments.
     */
    private final transient String[] arguments;

    /**
     * Ctor.
     * @param args Command line args
     */
    public Entry(final String[] args) {
        this.arguments = args;
    }

    /**
     * Main entry point.
     * @param args Arguments
     * @throws IOException If fails
     */
    public static void main(final String... args) throws IOException {
        new Entry(args).exec();
    }

    /**
     * Run it all.
     * @throws IOException If fails
     */
    public void exec() throws IOException {
        final Talks talks = new DyTalks(
            this.dynamo(), this.sttc().counters().get("rt-talk")
        );
        final Collection<Pulse.Tick> ticks = Collections.synchronizedCollection(
            EvictingQueue.<Pulse.Tick>create(
                (int) TimeUnit.HOURS.toMinutes(1L)
            )
        );
        final Routine routine = new Routine(
            talks, ticks, this.github(), this.sttc()
        );
        try {
            final App app = new App(talks, ticks, new Toggles());
            new FtCLI(app, this.arguments).start(Exit.NEVER);
        } finally {
            routine.close();
        }
    }

    /**
     * Make github.
     * @return Github
     */
    @Cacheable(forever = true)
    private Github github() {
        Logger.warn(this, "Github connected");
        return new RtGithub(
            new RtGithub(
                Manifests.read("Rultor-GithubToken")
            ).entry().through(RetryWire.class)
        );
    }

    /**
     * Sttc.
     * @return Sttc
     */
    @Cacheable(forever = true)
    private Sttc sttc() {
        Logger.warn(this, "Sttc connected");
        return new CdSttc(
            new ReSttc(
                RtSttc.make(
                    URN.create(Manifests.read("Rultor-SttcUrn")),
                    Manifests.read("Rultor-SttcToken")
                )
            )
        );
    }

    /**
     * Dynamo DB region.
     * @return Region
     */
    @Cacheable(forever = true)
    private Region dynamo() {
        final String key = Manifests.read("Rultor-DynamoKey");
        Credentials creds = new Credentials.Simple(
            key,
            Manifests.read("Rultor-DynamoSecret")
        );
        if (key.startsWith("AAAAA")) {
            final int port = Integer.parseInt(
                System.getProperty("dynamo.port")
            );
            creds = new Credentials.Direct(creds, port);
            Logger.warn(this, "test DynamoDB at port #%d", port);
        }
        return new Region.Prefixed(
            new ReRegion(new Region.Simple(creds)), "rt-"
        );
    }

}
