/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Repo;
import com.jcabi.github.User;
import com.jcabi.log.Logger;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Github crew.
 *
 * @since 1.40.7
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "repo")
final class Crew {

    /**
     * Github.
     */
    private final transient Repo repo;

    /**
     * Ctor.
     * @param rpo Github repo
     */
    Crew(final Repo rpo) {
        this.repo = rpo;
    }

    /**
     * Get all collaborators.
     * @return List of their login names
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Collection<String> names() {
        final Collection<String> names = new LinkedList<>();
        try {
            for (final User user : this.repo.collaborators().iterate()) {
                names.add(user.login());
            }
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            Logger.warn(
                this, "failed to fetch collaborator: %s",
                ex.getLocalizedMessage()
            );
        }
        return names;
    }

}
