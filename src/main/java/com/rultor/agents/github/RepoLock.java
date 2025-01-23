/*
 * Copyright (c) 2009-2025 Yegor Bugayenko
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
