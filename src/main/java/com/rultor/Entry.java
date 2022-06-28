/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor;

import co.stateful.RtSttc;
import co.stateful.Sttc;
import co.stateful.cached.CdSttc;
import co.stateful.retry.ReSttc;
import com.google.common.collect.EvictingQueue;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.LogExceptions;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.wire.RetryCarefulWire;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rultor.cached.CdTalks;
import com.rultor.dynamo.DyTalks;
import com.rultor.spi.Pulse;
import com.rultor.spi.Talks;
import com.rultor.spi.Tick;
import com.rultor.web.TkApp;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.takes.http.Exit;
import org.takes.http.FtCli;

/**
 * Command line entry.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.50
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class Entry {

    /**
     * Arguments.
     */
    private final transient Iterable<String> arguments;

    /**
     * Ctor.
     * @param args Command line args
     */
    public Entry(final String... args) {
        this.arguments = Arrays.asList(args);
    }

    /**
     * Main entry point.
     * @param args Arguments
     * @throws IOException If fails
     */
    @LogExceptions
    public static void main(final String... args) throws IOException {
        Logger.info(Entry.class, "Starting Rultor on the command line...");
        new Entry(args).exec();
    }

    /**
     * Run it all.
     * @throws IOException If fails
     */
    public void exec() throws IOException {
        final String dsn = Manifests.read("Rultor-SentryDsn");
        if (!dsn.startsWith("test")) {
            Sentry.init(dsn);
        }
        final Talks talks = new CdTalks(
            new DyTalks(
                this.dynamo(), this.sttc().counters().get("rt-talk")
            )
        );
        Logger.info(this, "Starting the Routine...");
        final Routine routine = new Routine(
            talks, Entry.pulse(), this.github(), this.sttc()
        );
        Logger.info(this, "Starting the web front to run forever...");
        try {
            new FtCli(
                new TkApp(talks, Entry.pulse(), new Toggles.InFile()),
                this.arguments
            ).start(Exit.NEVER);
        } finally {
            routine.close();
        }
    }

    /**
     * Make github.
     * @return Github
     * @throws IOException If fails
     */
    @Cacheable(forever = true)
    private Github github() throws IOException {
        Logger.info(this, "Connecting GitHub...");
        final String token = Manifests.read("Rultor-GithubToken");
        final Github github;
        if (token.startsWith("${")) {
            github = new MkGithub();
        } else {
            github = new RtGithub(
                new RtGithub(token).entry().through(
                    RetryCarefulWire.class,
                    Tv.HUNDRED
                )
            );
        }
        Logger.info(this, "GitHub object instantiated...");
        Logger.info(
            this, "GitHub connected as @%s",
            github.users().self().login()
        );
        return github;
    }

    /**
     * Sttc.
     * @return Sttc
     */
    @Cacheable(forever = true)
    private Sttc sttc() {
        Logger.info(this, "Connecting Sttc...");
        final Sttc sttc = new CdSttc(
            new ReSttc(
                RtSttc.make(
                    URN.create(Manifests.read("Rultor-SttcUrn")),
                    Manifests.read("Rultor-SttcToken")
                )
            )
        );
        Logger.info(this, "Sttc connected as %s", sttc);
        return sttc;
    }

    /**
     * Dynamo DB region.
     * @return Region
     */
    @Cacheable(forever = true)
    private Region dynamo() {
        Logger.info(this, "Connecting DynamoDB...");
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
        Logger.info(this, "DynamoDB connected as %s", key);
        return new Region.Prefixed(
            new ReRegion(new Region.Simple(creds)), "rt-"
        );
    }

    /**
     * Create pulse.
     * @return Pulse
     */
    @Cacheable(forever = true)
    private static Pulse pulse() {
        final Collection<Tick> ticks = Collections.synchronizedCollection(
            EvictingQueue.<Tick>create((int) TimeUnit.HOURS.toMinutes(1L))
        );
        final Collection<Throwable> error = Collections.synchronizedCollection(
            new ArrayList<Throwable>(1)
        );
        // @checkstyle AnonInnerLengthCheck (50 lines)
        return new Pulse() {
            @Override
            public void add(final Tick tick) {
                ticks.add(tick);
            }
            @Override
            public Iterable<Tick> ticks() {
                return Collections.unmodifiableCollection(ticks);
            }
            @Override
            public Iterable<Throwable> error() {
                return Collections.unmodifiableCollection(error);
            }
            @Override
            public void error(final Iterable<Throwable> errors) {
                error.clear();
                for (final Throwable err : errors) {
                    error.add(err);
                }
            }
        };
    }

}
