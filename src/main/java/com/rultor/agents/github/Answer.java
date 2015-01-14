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

import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Smarts;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Answer to post.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "comment")
public final class Answer {

    /**
     * Maximum messages from me.
     */
    private static final int MAX = 5;

    /**
     * Original comment.
     */
    private final transient Comment.Smart comment;

    /**
     * Ctor.
     * @param cmt Comment
     */
    public Answer(final Comment.Smart cmt) {
        this.comment = cmt;
    }

    /**
     * Post it..
     * @param msg Message
     * @param args Arguments
     * @throws IOException If fails
     */
    public void post(final String msg, final Object... args)
        throws IOException {
        final Issue issue = this.comment.issue();
        final List<Comment.Smart> comments = Lists.newArrayList(
            new Smarts<Comment.Smart>(issue.comments().iterate())
        );
        Collections.reverse(comments);
        final String self = issue.repo().github().users().self().login();
        int mine = 0;
        for (final Comment.Smart cmt : comments) {
            if (!cmt.author().login().equals(self)) {
                break;
            }
            ++mine;
        }
        if (mine < Answer.MAX) {
            issue.comments().post(this.msg(Logger.format(msg, args)));
        } else {
            Logger.error(
                this, "too many (%d) comments from %s already in %s#%d",
                mine, self, issue.repo().coordinates(), issue.number()
            );
        }
    }

    /**
     * Make a message to post.
     * @param text The text
     * @return Text to post
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private String msg(final String text) {
        String msg;
        try {
            msg = String.format(
                "> %s\n\n@%s %s",
                this.comment.body().replace("\n", " "),
                this.comment.author().login(),
                text
            );
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            msg = text;
        }
        return msg;
    }

}
