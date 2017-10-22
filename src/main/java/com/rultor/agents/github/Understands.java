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
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Bulk;
import com.jcabi.github.Comment;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Smarts;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.daemons.Home;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cactoos.iterable.Joined;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Understands request.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "github", "question" })
@SuppressWarnings
    (
        {
            "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity"
        }
    )
public final class Understands extends AbstractAgent {

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
            "/talk[@later='true']",
            "/talk/wire[github-repo and github-issue]"
        );
        this.github = ghub;
        this.question = qtn;
    }

    // @checkstyle ExecutableStatementCountCheck (50 lines)
    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final Iterator<Comment.Smart> comments = new SafeIterator<>(
            new Smarts<Comment.Smart>(
                new Joined<Comment>(
                    Collections.singleton(new FirstComment(issue)),
                    new Bulk<>(issue.comments().iterate(new Date(0L)))
                )
            ).iterator()
        );
        final int seen = Understands.seen(xml);
        int next = seen;
        int fresh = 0;
        int total = 0;
        Req req = Req.EMPTY;
        while (comments.hasNext()) {
            final Comment.Smart comment = comments.next();
            ++total;
            if (comment.number() <= seen) {
                continue;
            }
            ++fresh;
            req = this.parse(comment, xml);
            if (req.equals(Req.LATER)) {
                break;
            }
            next = comment.number();
            if (!req.equals(Req.EMPTY)) {
                break;
            }
        }
        if (next < seen) {
            throw new IllegalStateException(
                String.format(
                    "comment #%d is younger than #%d, something is wrong",
                    next, seen
                )
            );
        }
        final Directives dirs = new Directives();
        if (req.equals(Req.EMPTY)) {
            Logger.info(
                this, "nothing new in %s#%d, fresh/total: %d/%d",
                issue.repo().coordinates(), issue.number(), fresh, total
            );
        } else if (req.equals(Req.DONE)) {
            Logger.info(
                this, "simple request in %s#%d",
                issue.repo().coordinates(), issue.number()
            );
        } else if (req.equals(Req.LATER)) {
            Logger.info(
                this, "temporary pause in %s#%d, at message #%d",
                issue.repo().coordinates(), issue.number(), next
            );
        } else {
            dirs.xpath("/talk/request").remove()
                .xpath("/talk[not(request)]").strict(1)
                .add("request")
                .attr("id", Integer.toString(next))
                .append(req.dirs());
        }
        if (next > seen) {
            dirs.xpath("/talk/wire")
                .addIf("github-seen")
                .set(Integer.toString(next));
        }
        return dirs.xpath("/talk")
            .attr("later", Boolean.toString(!req.equals(Req.EMPTY)));
    }

    /**
     * Understand.
     * @param comment Comment
     * @param xml XML
     * @return Req
     * @throws IOException If fails
     */
    private Req parse(final Comment.Smart comment, final XML xml)
        throws IOException {
        Req req;
        try {
            req = this.question.understand(
                comment,
                new Home(xml, Integer.toString(comment.number())).uri()
            );
        } catch (final Profile.ConfigException ex) {
            new Answer(comment).post(
                false,
                String.format(
                    Understands.PHRASES.getString("Understands.broken-profile"),
                    ExceptionUtils.getRootCauseMessage(ex)
                )
            );
            req = Req.EMPTY;
        }
        return req;
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
