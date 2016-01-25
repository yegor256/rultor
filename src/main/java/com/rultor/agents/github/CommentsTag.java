/**
 * Copyright (c) 2009-2015, rultor.com
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Release;
import com.jcabi.github.Releases;
import com.jcabi.github.Repo;
import com.jcabi.github.Smarts;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.daemons.Home;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Comments a new tag in Github.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.31
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
        final Releases.Smart rels = new Releases.Smart(issue.repo().releases());
        final URI home = new Home(xml).uri();
        final List<DefaultArtifactVersion> priors =
                this.transformVersions(rels);
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
        } else if (VersionValidator.getInstance().isVersionValid(tag)
            && !VersionValidator.getInstance().isReleaseValid(tag, priors)) {
            issue.comments().post(
                    String.format(
                            CommentsTag.PHRASES.getString(
                                    "CommentsTag.version-too-low"
                            ),
                            tag,
                            Collections.max(priors)
                    )
            );
            Logger.info(
                    this,
                    "tag %s must be greater than previous version %s",
                    tag,
                    Collections.max(priors)
            );
        } else {
            final Repo repo = issue.repo();
            final Date prev = CommentsTag.previous(repo);
            final Release.Smart rel = new Release.Smart(
                rels.create(tag.trim())
            );
            rel.name(issue.title());
            rel.prerelease(true);
            rel.body(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "See #%d, release log:\n\n%s\n\nReleased by Rultor %s, see [build log](%s)",
                    issue.number(),
                    new CommitsLog(repo).build(prev, rel.publishedAt()),
                    Manifests.read("Rultor-Version"), home
                )
            );
            Logger.info(this, "tag %s created and commented", tag);
        }
        return new Directives();
    }

    /**
     * Transforms versionsFrom Release to DefaultArtificatVersion and filters
     * invalid version numbers. For example in these versions,
     * ["1.0", "2.0", "3.0-b"], "3.0-b" is just ignore, therefore version "2.0"
     * is the max.
     * @param rels All previous releases as Release objects
     * @return All prior releases wrapped in a DefaultArtifactVersion
     */
    private List<DefaultArtifactVersion> transformVersions(final Releases rels)
    {
        final Function<Release, DefaultArtifactVersion> releaseToVersions =
            new Function<Release, DefaultArtifactVersion>() {
            @Override
            public DefaultArtifactVersion apply(final Release release) {
                final Release.Smart rel = new Release.Smart(release);
                try {
                    final String tag = rel.tag();
                        return new DefaultArtifactVersion(tag);
                } catch (final IOException exception) {
                    Logger.error(this, "IOException caused from rel.tag()");
                    return null;
                }
            }
        };
        final Predicate<? super DefaultArtifactVersion> predicate =
                new Predicate<DefaultArtifactVersion>() {
            @Override
            public boolean apply(final DefaultArtifactVersion version) {
                return VersionValidator.getInstance().isVersionValid(
                        version.toString()
                );
            }
        };
        return FluentIterable
                .from(rels.iterate())
                .transform(releaseToVersions)
                .filter(predicate)
                .toList();
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
