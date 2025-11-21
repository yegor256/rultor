/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.GitHub;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Attach binaries to release.
 *
 * @since 1.3
 *
 * @todo #662:30min Implement attaching artifacts to release, remove PMD
 *  suppression below and enable attachesBinariesToRelease test. Similar
 *  functionality exists in CommentsTag so you can look there for clues.
 */
@Immutable
@ToString
public final class ReleaseBinaries extends AbstractAgent {
    /**
     * Ctor.
     * @param ghub GitHub client
     * @param prof Profile
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public ReleaseBinaries(final GitHub ghub, final Profile prof) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and type='release' and success='true']"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        return new Directives();
    }
}
