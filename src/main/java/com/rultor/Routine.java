/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import co.stateful.Sttc;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.aspects.Timeable;
import com.jcabi.github.GitHub;
import com.jcabi.log.Logger;
import com.rultor.agents.Agents;
import com.rultor.agents.github.qtn.DefaultBranch;
import com.rultor.profiles.Profiles;
import com.rultor.spi.Profile;
import com.rultor.spi.Pulse;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import com.rultor.spi.Tick;
import io.sentry.Sentry;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.constraints.NotNull;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;

/**
 * Routine.
 *
 * @since 1.50
 * @todo #1125:30min Routine should be delegate execution to separate threads.
 *  Currently com.rultor.Routine#process() is sequentially processing all Talks
 *  and breaking out of this sequential processing to log occurring exceptions.
 *  This leads to issues in one build breaking all builds globally.
 *  This should be reworked to run the chain of Agents for each talk in a
 *  separate thread, not interfering with the main Routine in error cases.
 *  Once this is done the swallowing of generic exceptions, added to
 *  circumvent this issue, in
 *  com.rultor.agents.github.Reports#process(com.jcabi.xml.XML) should be
 *  removed.
 */
@ScheduleWithFixedDelay
@SuppressWarnings({"PMD.DoNotUseThreads",
    "PMD.ConstructorShouldDoInitialization"})
final class Routine implements Runnable, Closeable {

    /**
     * How many talks to process in one cycle.
     */
    private static final int MAX_TALKS = 16;

    /**
     * Shutting down?
     */
    private final transient AtomicBoolean down = new AtomicBoolean();

    /**
     * When I started.
     */
    private final transient long start = System.currentTimeMillis();

    /**
     * Ticks.
     */
    private final transient Pulse pulse;

    /**
     * Talks.
     */
    private final transient Talks talks;

    /**
     * Agents.
     */
    private final transient Agents agents;

    /**
     * Ctor.
     * @param tlks Talks
     * @param pls Pulse
     * @param github GitHub client
     * @param sttc Sttc client
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    Routine(@NotNull final Talks tlks, final Pulse pls,
        final GitHub github, final Sttc sttc) {
        this.talks = tlks;
        this.pulse = pls;
        this.agents = new Agents(github, sttc);
    }

    @Override
    public void close() {
        this.down.set(true);
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void run() {
        final long begin = System.currentTimeMillis();
        try {
            final List<Talk> active = new ListOf<>(this.talks.active());
            Logger.info(
                this, "Start processing active talks %s...",
                new Mapped<>(
                    talk -> String.format("%s:%s", talk.name(), talk.number()),
                    active
                )
            );
            final int processed = this.unsafe(active);
            if (Logger.isInfoEnabled(this)) {
                Logger.info(
                    this,
                    "Processed %d active talks in %[ms]s, alive for %[ms]s: %tc",
                    processed,
                    System.currentTimeMillis() - begin,
                    System.currentTimeMillis() - this.start,
                    new Date()
                );
            }
            this.pulse.error(Collections.emptyList());
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            if (!this.down.get()) {
                Logger.error(this, "#run(): %[exception]s", ex);
                try {
                    TimeUnit.MICROSECONDS.sleep(1L);
                } catch (final InterruptedException iex) {
                    Logger.info(this, "%[exception]s", iex);
                }
            }
            Sentry.captureException(ex);
            this.pulse.error(Collections.singleton(ex));
        }
    }

    /**
     * Routine every-minute proc.
     * @param active List of active talks
     * @return Total talks processed
     * @throws IOException If fails
     */
    @Timeable(limit = 20, unit = TimeUnit.MINUTES)
    private int unsafe(final List<Talk> active) throws IOException {
        final long begin = System.currentTimeMillis();
        int total = 0;
        if (new Toggles.InFile().readOnly()) {
            Logger.info(this, "read-only mode");
        } else {
            total = this.process(active);
        }
        this.pulse.add(
            new Tick(begin, System.currentTimeMillis() - begin, total)
        );
        return total;
    }

    /**
     * Routine every-minute proc.
     * @param active List of active talks
     * @return Total talks processed
     * @throws IOException If fails
     */
    private int process(final List<Talk> active) throws IOException {
        this.agents.starter().execute(this.talks);
        final Profiles profiles = new Profiles();
        Collections.reverse(active);
        int total = 0;
        for (final Talk talk : active) {
            ++total;
            try {
                final Profile profile = profiles.fetch(talk);
                this.agents.agent(talk, profile).execute(talk);
                if (total > Routine.MAX_TALKS) {
                    break;
                }
            } catch (final DefaultBranch.RepoNotFoundException ex) {
                Logger.warn(this, "The repo not found: %[exception]s", ex);
                talk.active(false);
            }
        }
        this.agents.closer().execute(this.talks);
        return total;
    }

}
