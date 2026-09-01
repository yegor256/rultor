/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Repo;
import java.io.IOException;

/**
 * Default branch of a repo.
 * @since 2.1
 */
public final class DefaultBranch {

    /**
     * GitHub Repo.
     */
    private final transient Repo repo;

    /**
     * Ctor.
     * @param repository GitHub Repo
     */
    public DefaultBranch(final Repo repository) {
        this.repo = repository;
    }

    @Override
    public String toString() {
        try {
            return this.repo.defaultBranch().name();
        } catch (final IOException ex) {
            throw new IllegalStateException(
                String.format("Repo %s has no default branch", this.repo),
                ex
            );
        } catch (final AssertionError ex) {
            throw new RepoNotFoundException(
                this.repo.coordinates().toString(), ex
            );
        }
    }
}
