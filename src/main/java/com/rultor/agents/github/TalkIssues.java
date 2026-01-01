/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Coordinates;
import com.jcabi.github.GitHub;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Issues referenced from Talks.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "github", "xml" })
public final class TalkIssues {

    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * XML.
     */
    private final transient XML xml;

    /**
     * Ctor.
     * @param ghub GitHub client
     * @param talk Talk XML
     */
    public TalkIssues(final GitHub ghub, final XML talk) {
        this.github = ghub;
        this.xml = talk;
    }

    /**
     * Find and get issue.
     * @return Issue
     */
    public Issue.Smart get() {
        final XML wire = this.xml.nodes("/talk/wire").get(0);
        final Coordinates coords = new Coordinates.Simple(
            wire.xpath("github-repo/text()").get(0)
        );
        final Repo repo = this.github.repos().get(coords);
        return new Issue.Smart(
            repo.issues().get(
                Integer.parseInt(
                    wire.xpath("github-issue/text()").get(0)
                )
            )
        );
    }
}
