/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import co.stateful.Locks;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Repo;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.RepoLock;
import com.rultor.agents.github.Req;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Passes through only if it is alone in this repo.
 *
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "talk", "locks", "origin" })
public final class QnAlone implements Question {

    /**
     * Talk.
     */
    private final transient Talk talk;

    /**
     * Locks to use.
     */
    private final transient Locks locks;

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param tlk Talk
     * @param lcks Locks
     * @param qtn Original question
     */
    public QnAlone(final Talk tlk, final Locks lcks, final Question qtn) {
        this.talk = tlk;
        this.locks = lcks;
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Repo repo = comment.issue().repo();
        final Req req;
        final RepoLock lock = new RepoLock(this.locks, repo);
        if (lock.lock(this.talk)) {
            Logger.info(
                this, "%s locked by issue #%s, comment #%d",
                repo.coordinates(), comment.issue().number(), comment.number()
            );
            req = this.origin.understand(comment, home);
        } else {
            req = Req.LATER;
        }
        return req;
    }

}
