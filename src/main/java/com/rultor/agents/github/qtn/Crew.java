/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Repo;
import com.jcabi.github.User;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * GitHub crew.
 * @since 1.40.7
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "repo")
final class Crew {

    /**
     * GitHub.
     */
    private final transient Repo repo;

    /**
     * Ctor.
     * @param rpo GitHub repo
     */
    Crew(final Repo rpo) {
        this.repo = rpo;
    }

    /**
     * Get all collaborators.
     * @return List of their login names
     */
    Collection<String> names() {
        final Collection<String> names = new ArrayList<>(0);
        try {
            for (final User user : this.repo.collaborators().iterate()) {
                names.add(user.login());
            }
        } catch (final IOException | IllegalStateException ex) {
            Logger.warn(
                this, "failed to fetch collaborator: %s",
                ex.getLocalizedMessage()
            );
        }
        return names;
    }
}
