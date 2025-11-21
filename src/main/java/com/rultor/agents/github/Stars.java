/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Coordinates;
import com.jcabi.github.GitHub;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Stars repos used.
 * @since 1.0
 */
public final class Stars extends AbstractAgent {
    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * Ctor.
     * @param ghub GitHub client
     */
    public Stars(final GitHub ghub) {
        super("/talk/wire[github-repo]");
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Repo repo = this.github.repos().get(
            new Coordinates.Simple(
                xml.nodes("/talk/wire").get(0)
                    .xpath("github-repo/text()").get(0)
            )
        );
        if (!repo.stars().starred()) {
            repo.stars().star();
        }
        return new Directives();
    }
}
