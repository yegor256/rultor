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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Comment;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.JsonReadable;
import com.jcabi.github.Pull;
import com.jcabi.github.Smarts;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.TalkAgent;
import com.rultor.spi.Repo;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.net.URLEncoder;
import javax.json.JsonObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directives;

/**
 * Gets merge request via Github pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class GetsMergeRequest extends TalkAgent.Abstract {

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Reviewers.
     */
    private final transient Array<String> reviewers;

    /**
     * Ctor.
     * @param ghub Github client
     * @param revs Reviewers
     */
    public GetsMergeRequest(final Repo rpo, final Github ghub,
        final Iterable<String> revs) {
        super();
        this.repo = rpo;
        this.github = ghub;
        this.reviewers = new Array<String>(revs);
    }

    @Override
    protected void process(final Talk talk, final XML xml) throws IOException {
        final Issue.Smart issue = new TalkIssues(this.github).get(talk);
        final int seen;
        if (xml.nodes("/talk/wire/github-seen").isEmpty()) {
            seen = 0;
        } else {
            seen = Integer.parseInt(
                xml.xpath("/talk/wire/github-seen/text()").get(0)
            );
        }
        final Iterable<Comment.Smart> comments = new Smarts<Comment.Smart>(
            issue.comments().iterate()
        );
        final String prefix = String.format(
            "@%s ", this.github.users().self().login()
        );
        int next = seen;
        boolean found = false;
        for (final Comment.Smart comment : comments) {
            if (comment.number() <= seen) {
                continue;
            }
            next = comment.number();
            if (!comment.body().startsWith(prefix)) {
                Logger.info(
                    this,
                    // @checkstyle LineLength (1 line)
                    "comment #%d ignored, it is not addressed to me (doesn't start with \"%s\")",
                    comment.number(), prefix
                );
                continue;
            }
            if (!comment.body().contains("good to merge")) {
                Logger.info(
                    this, "comment #%d is not about merging: %s",
                    comment.number(), comment.body()
                );
                continue;
            }
            if (found) {
                Logger.info(
                    this, "merge request already found, enough for one issue"
                );
                continue;
            }
            if (!xml.xpath("/talk/merge-request-git").isEmpty()) {
                new Answer(comment).post(
                    "I'm busy with another merge request"
                );
                continue;
            }
            found = this.read(talk, comment);
        }
        if (next > seen) {
            talk.modify(
                new Directives().xpath("/talk/wire")
                    .addIf("github-seen").set(Integer.toString(next)),
                String.format("messages up to #%d seen", next)
            );
        }
    }

    /**
     * Process/read one comment.
     * @param talk Talk
     * @param comment The comment
     * @return TRUE if request found
     * @throws IOException
     */
    private boolean read(final Talk talk, final Comment.Smart comment)
        throws IOException {
        final boolean found;
        if (this.reviewers.contains(comment.author().login())) {
            final String hash = DigestUtils.md5Hex(
                RandomStringUtils.random(Tv.FIVE)
            ).substring(0, Tv.EIGHT);
            talk.modify(
                GetsMergeRequest.dirs(comment.issue(), hash),
                String.format("merge request in #%d", comment.issue().number())
            );
            new Answer(comment).post(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "OK, I'm on it. You can track me [here](http://www.rultor.com/d/%d/%s/%s)",
                    this.repo.number(),
                    URLEncoder.encode(talk.name(), CharEncoding.UTF_8),
                    hash
                )
            );
            found = true;
        } else {
            new Answer(comment).post(
                String.format(
                    "Thanks, but I need a confirmation from one of them: %s",
                    StringUtils.join(
                        Iterables.transform(
                            this.reviewers,
                            new Function<String, Object>() {
                                @Override
                                public Object apply(final String input) {
                                    return String.format("@%s", input);
                                }
                            }
                        ),
                        ", "
                    )
                )
            );
            found = false;
        }
        return found;
    }

    /**
     * Make dirs from pull request.
     * @param issue Issue with PR
     * @param hash Hash code of the request
     * @return Dirs
     */
    private static Directives dirs(final Issue issue, final String hash)
        throws IOException {
        final JsonReadable pull = new Pull.Smart(
            issue.repo().pulls().get(issue.number())
        );
        final JsonObject head = pull.json().getJsonObject("head");
        final JsonObject base = pull.json().getJsonObject("base");
        return new Directives()
            .xpath("/talk[not(merge-request-git)]").strict(1)
            .add("merge-request-git")
            .attr("id", hash)
            .add("base")
            .set(
                String.format(
                    "git@github.com:%s.git",
                    base.getJsonObject("repo").getString("full_name")
                )
            )
            .up()
            .add("base-branch").set(base.getString("ref")).up()
            .add("head")
            .set(
                String.format(
                    "git@github.com:%s.git",
                    head.getJsonObject("repo").getString("full_name")
                )
            )
            .up()
            .add("head-branch").set(head.getString("ref"));
    }

}
