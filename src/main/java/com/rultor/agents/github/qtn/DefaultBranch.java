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
