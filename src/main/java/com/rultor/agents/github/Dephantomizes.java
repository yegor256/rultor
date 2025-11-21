/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.GitHub;
import com.jcabi.github.Issue;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Removes request, if GitHub issue is gone.
 *
 * @since 1.59.7
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "github")
public final class Dephantomizes extends AbstractAgent {

    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * Ctor.
     * @param ghub GitHub client
     */
    public Dephantomizes(final GitHub ghub) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id]"
        );
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Directives dirs = new Directives();
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        if (!issue.exists()) {
            dirs.xpath("/talk").push()
                .xpath("request").remove().pop()
                .xpath("wire").remove();
            Logger.warn(
                this, "issue #%d in %s is gone, deleting request",
                issue.number(), issue.repo().coordinates()
            );
        }
        return dirs;
    }

}
