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
package com.rultor.agents.hn;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.github.TalkIssues;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Hacker News Update.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.58
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "github", "hnews" })
public final class HnUpdates extends AbstractAgent {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Hacker news.
     */
    private final transient HackerNews hnews;

    /**
     * Ctor.
     * @param ghub Github client
     * @param news Hacker News client
     */
    public HnUpdates(final Github ghub, final HackerNews news) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and type='release' and success='true']"
        );
        this.github = ghub;
        this.hnews = news;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("/talk/request").get(0);
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final Repo.Smart repo = new Repo.Smart(issue.repo());
        if (!repo.isPrivate()) {
            this.post(repo, req.xpath("args/arg[@name='tag']/text()").get(0));
        }
        return new Directives();
    }

    /**
     * Create a post to HN.
     * @param repo The repo
     * @param tag The tag
     * @throws IOException If fails
     */
    private void post(final Repo.Smart repo, final String tag)
        throws IOException {
        final StringBuilder text = new StringBuilder(2 * Tv.HUNDRED);
        text.append(tag).append(" released, ");
        if (repo.description().isEmpty()) {
            text.append(repo.coordinates().repo());
        } else {
            text.append(
                StringUtils.substring(
                    repo.description(),
                    0, Tv.HUNDRED
                )
            );
        }
        this.hnews.post(
            String.format("https://github.com/%s", repo.coordinates()),
            text.toString()
        );
        Logger.info(
            this, "posted to Hacker News: \"%s\" (%s)",
            text, repo.coordinates()
        );
    }

}
