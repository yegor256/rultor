/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Release;
import com.jcabi.github.Releases;
import com.jcabi.github.Repo;
import com.jcabi.github.Smarts;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.Env;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.daemons.Home;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Comments a new tag in Github.
 *
 * @since 1.31
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "github")
@SuppressWarnings("PMD.ExcessiveImports")
public final class CommentsTag extends AbstractAgent {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param ghub Github client
     */
    public CommentsTag(final Github ghub) {
        this(ghub, Profile.EMPTY);
    }

    /**
     * Constructor.
     * @param ghub Github client
     * @param config Profile
     */
    public CommentsTag(
        final Github ghub,
        final Profile config
    ) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and type='release' and success='true']"
        );
        this.github = ghub;
        this.profile = config;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("/talk/request").get(0);
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final String tag = req.xpath("args/arg[@name='tag']/text()").get(0);
        final Releases.Smart rels = new Releases.Smart(issue.repo().releases());
        final URI home = new Home(xml).uri();
        if (rels.exists(tag)) {
            final Release.Smart rel = new Release.Smart(rels.find(tag));
            rel.body(
                String.format(
                    "%s\n\nSee also #%d and [build log](%s)",
                    rel.body(), issue.number(), home
                )
            );
            issue.comments().post(
                String.format(
                    CommentsTag.PHRASES.getString("CommentsTag.duplicate"),
                    tag
                )
            );
            Logger.info(this, "duplicate tag %s commented", tag);
        } else {
            final Repo repo = issue.repo();
            final Date prev = CommentsTag.previous(repo);
            final Release.Smart rel = new Release.Smart(
                rels.create(tag.trim())
            );
            rel.name(CommentsTag.title(req, issue));
            rel.prerelease(this.isPrerelease(req));
            rel.body(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "See #%d, release log:\n\n%s\n\nReleased by Rultor %s, see [build log](%s)",
                    issue.number(),
                    new CommitsLog(repo).build(prev, rel.publishedAt()),
                    Env.read("Rultor-Version"), home
                )
            );
            Logger.info(this, "tag %s created and commented", tag);
        }
        return new Directives();
    }

    /**
     * Check if release is prerelease.
     * True if profile does not specify release.pre=false.
     * @param req Comment's xml
     * @return True if prerelease, false otherwise.
     */
    private boolean isPrerelease(final XML req) {
        try {
            final boolean result;
            final List<String> comment = req
                .xpath("args/arg[@name='pre']/text()");
            if (comment.isEmpty()) {
                final List<String> xpath = this.profile.read()
                    .xpath("/p/entry[@key='release']/entry[@key='pre']/text()");
                if (xpath.isEmpty()) {
                    result = true;
                } else {
                    result = "true".equals(xpath.get(0).trim());
                }
            } else {
                result = "true".equals(comment.get(0).trim());
            }
            return result;
        } catch (final IOException exception) {
            throw new IllegalStateException(
                String.format(
                    "Can't read profile: '%s' to determine pre-release status",
                    this.profile
                ),
                exception
            );
        }
    }

    /**
     * Get title.
     * @param request Request to get title from
     * @param issue Issue to get title from as fallback
     * @return Title
     * @throws IOException In case of problem communicating with repo.
     */
    private static String title(
        final XML request,
        final Issue.Smart issue
    ) throws IOException {
        final Optional<String> title = request.xpath(
            "args/arg[@name='title']/text()"
        ).stream().findFirst();
        final String res;
        if (title.isPresent()) {
            res = title.get();
        } else {
            res = issue.title();
        }
        return res;
    }

    /**
     * Get previous release time.
     * @param repo Repo in which to find the releases.
     * @return Previous release time or start of epoch.
     * @throws IOException In case of problem communicating with repo.
     */
    private static Date previous(final Repo repo) throws IOException {
        Date prev = new Date(0L);
        final Iterable<Release.Smart> releases =
            new Smarts<>(repo.releases().iterate());
        for (final Release.Smart rel : releases) {
            if (rel.json().isNull("published_at")) {
                continue;
            }
            if (prev.before(rel.publishedAt())) {
                prev = rel.publishedAt();
            }
        }
        return prev;
    }
}
