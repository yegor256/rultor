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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Bulk;
import com.jcabi.github.Comment;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Smarts;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.daemons.Home;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Understands request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "github", "question" })
public final class Understands extends AbstractAgent {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Question.
     */
    private final transient Question question;

    /**
     * Ctor.
     * @param ghub Github client
     * @param qtn Question
     */
    public Understands(final Github ghub, final Question qtn) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk[not(request)]"
        );
        this.github = ghub;
        this.question = qtn;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final Iterable<Comment.Smart> comments = new Smarts<Comment.Smart>(
            new Bulk<Comment>(issue.comments().iterate())
        );
        final int seen = Understands.seen(xml);
        int next = seen;
        Req req = Req.EMPTY;
        final String hash = DigestUtils.md5Hex(
            Long.toString(System.currentTimeMillis())
        ).substring(0, Tv.EIGHT);
        final URI home = new Home(xml, hash).uri();
        for (final Comment.Smart comment : comments) {
            if (comment.number() <= seen) {
                continue;
            }
            next = comment.number();
            req = this.question.understand(comment, home);
            if (!req.equals(Req.EMPTY)) {
                break;
            }
        }
        final Directives dirs = new Directives();
        if (!req.equals(Req.EMPTY)) {
            dirs.xpath("/talk").add("request").attr("id", hash)
                .append(req.dirs());
        }
        if (next > seen) {
            dirs.xpath("/talk/wire")
                .addIf("github-seen")
                .set(Integer.toString(next));
        }
        return dirs;
    }

    /**
     * Last seen message.
     * @param xml XML
     * @return Number
     */
    private static int seen(final XML xml) {
        final int seen;
        if (xml.nodes("/talk/wire/github-seen").isEmpty()) {
            seen = 0;
        } else {
            seen = Integer.parseInt(
                xml.xpath("/talk/wire/github-seen/text()").get(0)
            );
        }
        return seen;
    }

}
