/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import co.stateful.Locks;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.GitHub;
import com.jcabi.github.Issue;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.text.Joined;

/**
 * Unlocks repo.
 *
 * @since 1.8.12
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "locks", "github" })
public final class UnlocksRepo implements SuperAgent {

    /**
     * Which talks should be unlocked.
     */
    private static final String XPATH = new Joined(
        "",
        "/talk[not(request) and not(daemon) and not(shell)",
        " and wire/github-repo and wire/github-issue]"
    ).toString();

    /**
     * Locks.
     */
    private final transient Locks locks;

    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * Ctor.
     * @param lcks Locks
     * @param ghub GitHub client
     */
    public UnlocksRepo(final Locks lcks, final GitHub ghub) {
        this.locks = lcks;
        this.github = ghub;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        for (final Talk talk : talks.active()) {
            this.unlock(talk);
        }
    }

    /**
     * Unlock.
     * @param talk Talk
     * @throws IOException If fails
     */
    private void unlock(final Talk talk) throws IOException {
        final XML xml = talk.read();
        if (!xml.nodes(UnlocksRepo.XPATH).isEmpty()) {
            final Issue issue = new TalkIssues(this.github, talk.read()).get();
            if (new RepoLock(this.locks, issue.repo()).unlock(talk)) {
                Logger.info(
                    this, "%s unlocked by %s",
                    issue.repo().coordinates(), talk.name()
                );
            }
        }
    }

}
