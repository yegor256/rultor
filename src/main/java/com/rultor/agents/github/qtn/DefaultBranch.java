package com.rultor.agents.github.qtn;

import com.jcabi.github.Repo;
import java.io.IOException;

public class DefaultBranch {

    private final Repo repo;

    public DefaultBranch(final Repo repo) {
        this.repo = repo;
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
        }
    }
}
