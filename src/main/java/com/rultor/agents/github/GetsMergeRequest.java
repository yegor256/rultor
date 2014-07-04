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
import com.jcabi.github.Comment;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.JsonReadable;
import com.jcabi.github.Pull;
import com.jcabi.github.Smarts;
import com.jcabi.immutable.Array;
import com.jcabi.xml.XML;
import com.rultor.agents.TalkAgent;
import com.rultor.spi.Talk;
import java.io.IOException;
import javax.json.JsonObject;
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
public final class GetsMergeRequest implements TalkAgent {

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
    public GetsMergeRequest(final Github ghub, final Iterable<String> revs) {
        this.github = ghub;
        this.reviewers = new Array<String>(revs);
    }

    @Override
    public void execute(final Talk talk) throws IOException {
        final Issue.Smart issue = new TalkIssues(this.github).get(talk);
        final XML xml = talk.read();
        int seen;
        if (xml.nodes("/talk/wire/github-seen").isEmpty()) {
            seen = 0;
        } else {
            seen = Integer.parseInt(xml.xpath("/talk/wire/github-seen").get(0));
        }
        final Iterable<Comment.Smart> comments = new Smarts<Comment.Smart>(
            issue.comments().iterate()
        );
        final String prefix = String.format("@%s ", this.github.users().self());
        for (final Comment.Smart comment : comments) {
            if (comment.number() <= seen) {
                continue;
            }
            seen = comment.number();
            if (!comment.body().startsWith(prefix)) {
                continue;
            }
            if (!comment.body().contains("good to merge")) {
                continue;
            }
            this.read(talk, comment);
        }
        talk.modify(
            new Directives().xpath("/talk/wire/github-seen").set(
                Integer.toString(seen)
            )
        );
    }

    /**
     * Process/read one comment.
     * @param talk Talk
     * @param comment The comment
     * @throws IOException
     */
    public void read(final Talk talk, final Comment.Smart comment)
        throws IOException {
        if (this.reviewers.contains(comment.author().login())) {
            final JsonReadable pull = new Pull.Smart(
                comment.issue().repo().pulls().get(comment.number())
            );
            final JsonObject head = pull.json().getJsonObject("head");
            final JsonObject base = pull.json().getJsonObject("base");
            talk.modify(
                new Directives()
                    .xpath("/talk[not(merge-request-git)]").strict(1)
                    .add("merge-request-git")
                    .add("base")
                    .set(
                        String.format(
                            "git@github.com:%s",
                            base.getJsonObject("repo").get("full_name")
                        )
                    )
                    .up()
                    .add("base-branch").set(base.getString("ref")).up()
                    .add("head")
                    .set(
                        String.format(
                            "git@github.com:%s",
                            head.getJsonObject("repo").get("full_name")
                        )
                    )
                    .up()
                    .add("head-branch").set(head.getString("ref")).up()
                    .set("master").up()
            );
        } else {
            comment.issue().comments().post(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "> %s\n\nThanks, but I need a confirmation from one of them: %s",
                    comment.body().replace("\n", " "),
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
        }
    }

}
