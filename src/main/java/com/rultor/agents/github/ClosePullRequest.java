/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.GitHub;
import com.jcabi.github.Issue;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Closes pull request manually, leaves comment with description.
 *
 * @since 1.63
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = {"github", "profile"})
public final class ClosePullRequest extends AbstractAgent {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Profile.
     */
    private final transient Profile.Defaults profile;

    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * Constructor.
     * @param prof Profile
     * @param ghub GitHub
     */
    public ClosePullRequest(final Profile prof, final GitHub ghub) {
        super("/talk/wire[github-repo and github-issue]");
        this.profile = new Profile.Defaults(prof);
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String rebase = this.profile.text(
            "/p/entry[@key='merge']/entry[@key='rebase']",
            "false"
        );
        if ("true".equals(rebase)) {
            final Issue.Smart issue = new TalkIssues(this.github, xml).get();
            issue.close();
            issue.comments().post(
                ClosePullRequest.PHRASES.getString(
                    "ClosePullRequest.explanation"
                )
            );
        }
        return new Directives();
    }
}
