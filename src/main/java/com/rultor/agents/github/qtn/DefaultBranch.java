/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Repo;
import java.io.IOException;

/**
 * Default branch of a repo.
 *
 * @since 2.1
 */
public final class DefaultBranch {

    /**
     * GitHub Repo.
     */
    private final transient Repo repo;

    /**
     * Ctor.
     * @param repository GitHub Repo.
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
            throw new RepoNotFoundException(this.repo.coordinates().toString(), ex);
        }
    }

    /**
     * When repo is not found.
     * @since 2.1
     */
    public static class RepoNotFoundException extends RuntimeException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -3860028281726793188L;

        /**
         * Ctor.
         * @param name Name of repo
         * @param exp Original problem
         */
        public RepoNotFoundException(final String name, final Throwable exp) {
            super(String.format("Most probably the repo %s doesn't exist", name), exp);
        }
    }
}
