/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import co.stateful.RtSttc;
import co.stateful.Sttc;
import co.stateful.cached.CdSttc;
import co.stateful.retry.ReSttc;
import com.google.common.collect.EvictingQueue;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.LogExceptions;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.github.GitHub;
import com.jcabi.github.RtGitHub;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.github.wire.RetryCarefulWire;
import com.jcabi.log.Logger;
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
import org.cactoos.scalar.IoChecked;
import org.cactoos.scalar.LengthOf;
import org.takes.http.Exit;
import org.takes.http.FtCli;

/**
 * Command line entry.
 *
 * @since 1.50
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
        final String dsn = Env.read("Rultor-SentryDsn");
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
     * @return GitHub
     * @throws IOException If fails
     */
    @Cacheable(forever = true)
    private GitHub github() throws IOException {
        Logger.info(this, "Connecting GitHub...");
        final String token = Env.read("Rultor-GithubToken");
        final GitHub github;
        if (token.startsWith("${")) {
            github = new MkGitHub();
        } else {
            github = new RtGitHub(
                new RtGitHub(token).entry().through(
                    RetryCarefulWire.class,
                    100
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
     * @throws IOException If fails
     */
    @Cacheable(forever = true)
    private Sttc sttc() throws IOException {
        Logger.info(this, "Connecting Sttc...");
        final Sttc sttc = new CdSttc(
            new ReSttc(
                RtSttc.make(
                    URN.create(Env.read("Rultor-SttcUrn")),
                    Env.read("Rultor-SttcToken")
                )
            )
        );
        Logger.info(
            this, "There are %d counters for me in Sttc",
            new IoChecked<>(new LengthOf(sttc.counters().names())).value()
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
        final String key = Env.read("Rultor-DynamoKey");
        Credentials creds = new Credentials.Simple(
            key,
            Env.read("Rultor-DynamoSecret")
        );
        if (key.startsWith("AAAAA")) {
            final int port = Integer.parseInt(
                System.getProperty("failsafe.ddl.port")
            );
            creds = new Credentials.Direct(
                Credentials.Simple.class.cast(creds), port
            );
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
            EvictingQueue.create((int) TimeUnit.HOURS.toMinutes(1L))
        );
        final Collection<Throwable> error = Collections.synchronizedCollection(
            new ArrayList<>(1)
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
