/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.GitHub;
import com.jcabi.github.Issue;
import com.jcabi.github.safe.SfComment;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.daemons.Home;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Posts merge results to GitHub pull request.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "github")
public final class Reports extends AbstractAgent {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * Ctor.
     * @param ghub GitHub client
     */
    public Reports(final GitHub ghub) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and success]"
        );
        this.github = ghub;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("/talk/request").get(0);
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final boolean success = Boolean.parseBoolean(
            req.xpath("success/text()").get(0)
        );
        final URI home = new Home(xml).uri();
        final String pattern;
        if (success) {
            pattern = "Reports.success";
        } else {
            pattern = "Reports.failure";
        }
        final long number = Long.parseLong(req.xpath("@id").get(0));
        final Comment.Smart comment = new Comment.Smart(
            new SfComment(
                Reports.origin(issue, number)
            )
        );
        final StringBuilder message = new StringBuilder();
        if (comment.body().contains("stop")) {
            message.append(Reports.PHRASES.getString("Reports.stop-fails"))
                .append(' ');
        }
        message.append(
            Logger.format(
                Reports.PHRASES.getString(pattern),
                home.toASCIIString(),
                Long.parseLong(req.xpath("msec/text()").get(0))
            )
        ).append(Reports.highlights(req));
        if (!success) {
            message.append(Reports.tail(req));
        }
        new Answer(comment).post(success, message.toString());
        Logger.info(this, "issue #%d reported: %B", issue.number(), success);
        return new Directives()
            .xpath("/talk/request[success]")
            .strict(1).remove();
    }

    /**
     * Get highlights.
     * @param req Request
     * @return Highlights
     */
    private static String highlights(final XML req) {
        final List<String> highlights = req.xpath("highlights/text()");
        final String text;
        if (highlights.isEmpty()) {
            text = "";
        } else {
            text = String.format("\n\n%s", highlights.get(0));
        }
        return text;
    }

    /**
     * Get tail.
     * @param req Request
     * @return Tail
     */
    private static String tail(final XML req) {
        final List<String> tail = req.xpath("tail/text()");
        final String text;
        if (tail.isEmpty()) {
            text = "";
        } else {
            text = String.format(
                "\n\n```\n%s\n```",
                tail.get(0).replaceAll("```", "'''")
            );
        }
        return text;
    }

    /**
     * Get a comment we're answering to.
     * @param issue The issue
     * @param number Its number
     * @return Comment
     */
    private static Comment.Smart origin(final Issue.Smart issue,
        final long number) {
        final Comment comment;
        if (number == 1) {
            comment = new FirstComment(issue);
        } else {
            comment = issue.comments().get(number);
        }
        return new Comment.Smart(comment);
    }

}
