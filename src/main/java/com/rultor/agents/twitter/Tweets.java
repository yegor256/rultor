/**
 * Copyright (c) 2009-2018, rultor.com
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
package com.rultor.agents.twitter;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Language;
import com.jcabi.github.Repo;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.github.TalkIssues;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.text.SubText;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Tweets.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.30
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "github", "twitter" })
public final class Tweets extends AbstractAgent {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Twitter.
     */
    private final transient Twitter twitter;

    /**
     * Ctor.
     * @param ghub Github client
     * @param twt Twitter client
     */
    public Tweets(final Github ghub, final Twitter twt) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and type='release' and success='true']"
        );
        this.github = ghub;
        this.twitter = twt;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("/talk/request").get(0);
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final Repo.Smart repo = new Repo.Smart(issue.repo());
        if (!repo.isPrivate()) {
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

    /**
     * Create a tweet to post.
     * @param repo The repo
     * @param tag The tag
     * @return Tweet text
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.InsufficientStringBufferDeclaration")
    private static String tweet(final Repo.Smart repo, final String tag)
        throws IOException {
        final StringBuilder text = new StringBuilder(2 * Tv.HUNDRED);
        if (repo.hasDescription() && !repo.description().isEmpty()) {
            text.append(
                new SubText(
                    repo.description(),
                    0,
                    Tv.HUNDRED
                ).asString()
            );
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
