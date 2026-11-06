/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.twitter;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.GitHub;
import com.jcabi.github.Issue;
import com.jcabi.github.Language;
import com.jcabi.github.Repo;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.github.TalkIssues;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Tweets.
 *
 * @since 1.30
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "github", "twitter", "profile" })
public final class Tweets extends AbstractAgent {

    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * Twitter.
     */
    private final transient Twitter twitter;

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     *
     * @param ghub GitHub client
     * @param twt Twitter client
     */
    public Tweets(final GitHub ghub, final Twitter twt) {
        this(ghub, twt, Profile.EMPTY);
    }

    /**
     * Ctor.
     *
     * @param ghub GitHub client
     * @param twt Twitter client
     * @param config Profile
     */
    public Tweets(final GitHub ghub, final Twitter twt, final Profile config) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and type='release' and success='true']"
        );
        this.github = ghub;
        this.twitter = twt;
        this.profile = config;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("/talk/request").get(0);
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final Repo.Smart repo = new Repo.Smart(issue.repo());
        if (!repo.isPrivate() && this.tweetEnabled()) {
            this.twitter.post(
                Tweets.tweet(
                    repo, req.xpath("args/arg[@name='tag']/text()").get(0)
                )
            );
            Logger.info(
                this, "tweet posted about %s release",
                issue.repo().coordinates()
            );
        }
        return new Directives();
    }

    private boolean tweetEnabled() {
        try {
            final List<String> values = this.profile.read()
                .xpath("/p/entry[@key='release']/entry[@key='tweet']/text()");
            final boolean result;
            if (values.isEmpty()) {
                result = true;
            } else {
                result = "true".equals(values.get(0).trim());
            }
            return result;
        } catch (final IOException exception) {
            throw new IllegalStateException(
                String.format(
                    "Can't read profile: '%s' to determine tweet status",
                    this.profile
                ),
                exception
            );
        }
    }

    private static String tweet(final Repo.Smart repo, final String tag)
        throws IOException {
        final StringBuilder text = new StringBuilder(200);
        if (repo.hasDescription() && !repo.description().isEmpty()) {
            if (repo.description().length() > 100) {
                text.append(repo.description(), 0, 100);
            } else {
                text.append(repo.description());
            }
        } else {
            text.append(repo.coordinates().repo());
        }
        text.append(", ").append(tag)
            .append(" released https://github.com/")
            .append(repo.coordinates());
        for (final Language lang : repo.languages()) {
            text.append(String.format(" #%s", lang.name()));
        }
        return text.toString();
    }
}
