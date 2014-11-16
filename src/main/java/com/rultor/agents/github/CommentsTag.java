/**
 * Copyright (c) 2009-2014, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.agents.github;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Release;
import com.jcabi.github.Repo;
import com.jcabi.github.RepoCommit;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Comments a new tag in Github.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.31
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "github")
public final class CommentsTag extends AbstractAgent {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Ctor.
     * @param ghub Github client
     */
    public CommentsTag(final Github ghub) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and type='release' and success='true']"
        );
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("/talk/request").get(0);
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final String tag = req.xpath("args/arg[@name='tag']/text()").get(0);
        final Repo repo = issue.repo();
        final Date prev = this.previous(repo);
        final Release.Smart release = new Release.Smart(
            repo.releases().create(tag)
        );
        release.name(issue.title());
        release.body(this.body(repo, prev, release.publishedAt()));
        Logger.info(this, "tag %s commented", tag);
        return new Directives();
    }

    /**
     * Release body text.
     * @param repo Repo with releases.
     * @param prev Previous release date.
     * @param current Current release date.
     * @return Release body text.
     * @throws IOException In case of problem communicating with git.
     */
    private String body(final Repo repo, final Date prev, final Date current)
        throws IOException {
        final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'", Locale.ENGLISH
        );
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Collection<String> lines = new LinkedList<String>();
        lines.add("Following commits included in this release:");
        final ImmutableMap<String, String> params =
            new ImmutableMap.Builder<String, String>()
                .put("since", format.format(prev))
                .put("until", format.format(current))
                .build();
        for (final RepoCommit commit : repo.commits().iterate(params)) {
            lines.add(commit.sha());
        }
        return Joiner.on("\n").join(lines);
    }

    /**
     * Get previous release time.
     * @param repo Repo in which to find the releases.
     * @return Previous release time or start of epoch.
     * @throws IOException In case of problem communicating with repo.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Date previous(final Repo repo) throws IOException {
        Date prev = new Date(0);
        for (final Release release : repo.releases().iterate()) {
            final Release.Smart rel = new Release.Smart(release);
            if (prev.before(rel.publishedAt())) {
                prev = rel.publishedAt();
            }
        }
        return prev;
    }
}
