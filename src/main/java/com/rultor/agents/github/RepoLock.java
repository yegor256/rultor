/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import co.stateful.Lock;
import co.stateful.Locks;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Repo;
import com.jcabi.log.Logger;
import com.rultor.spi.Talk;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Repo lock.
 *
 * <p>It is used by {@link com.rultor.agents.github.qtn.QnAlone}
 * and {@link UnlocksRepo}.</p>
 *
 * @since 1.8.12
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "locks", "repo" })
public final class RepoLock {

    /**
     * Locks.
     */
    private final transient Locks locks;

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Ctor.
     * @param lcks Locks
     * @param rpo Repo
     */
    public RepoLock(final Locks lcks, final Repo rpo) {
        this.locks = lcks;
        this.repo = rpo;
    }

    /**
     * Lock.
     * @param talk Talk
     * @return TRUE if locked
     * @throws IOException If fails
     */
    public boolean lock(final Talk talk) throws IOException {
        final String name = RepoLock.label(talk);
        final boolean done = this.lock().lock(name);
        Logger.debug(this, "lock of %s: %B", name, done);
        return done;
    }

    /**
     * Unlock.
     * @param talk Talk
     * @return TRUE if unlocked
     * @throws IOException If fails
     */
    public boolean unlock(final Talk talk) throws IOException {
        final String name = RepoLock.label(talk);
        final boolean done = this.lock().unlock(name);
        Logger.debug(this, "unlock of %s: %B", name, done);
        return done;
    }

    /**
     * Get lock.
     * @return Lock
     * @throws IOException If fails
     */
    private Lock lock() throws IOException {
        return this.locks.get(
            String.format("rt-repo-%s", this.repo.coordinates()).replaceAll(
                "[^a-zA-Z0-9\\-]", "-"
            )
        );
    }

    /**
     * Get label.
     * @param talk Talk
     * @return Label
     * @throws IOException If fails
     */
    private static String label(final Talk talk) throws IOException {
        return talk.read().xpath("/talk/@name").get(0);
    }
}
