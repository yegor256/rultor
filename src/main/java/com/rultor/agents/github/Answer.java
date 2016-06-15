/**
 * Copyright (c) 2009-2016, rultor.com
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
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "msg")
public final class Answer {

    /**
     * Maximum messages from me.
     */
    private static final int MAX = 5;

    /**
     * The message that rultor sends.
     */
    private final transient Message msg;

    /**
     * Ctor.
     * @param message Message
     */
    public Answer(final Message message) {
        this.msg = message;
    }

    /**
     * Post it..
     * @throws IOException If fails
     */
    public void post() throws IOException {
        final Issue issue = this.msg.comment().issue();
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
            issue.comments().post(this.msg.body());
        } else {
            Logger.error(
                this, "too many (%d) comments from %s already in %s#%d",
                mine, self, issue.repo().coordinates(), issue.number()
            );
        }
    }

}
