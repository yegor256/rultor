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
import com.jcabi.aspects.Tv;
import com.jcabi.github.Comment;
import com.jcabi.github.Comment.Smart;
import com.jcabi.github.Issue;
import com.jcabi.github.Smarts;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Reversed;
import org.cactoos.list.SolidList;
import org.cactoos.text.JoinedText;
import org.xembly.Xembler;

/**
 * Answer to post.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
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
     * Space char.
     */
    private static final String SPACE = " ";

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
     * @param success Is it a report about success?
     * @param msg Message
     * @param args Arguments
     * @throws IOException If fails
     */
    public void post(final boolean success, final String msg,
        final Object... args) throws IOException {
        final Issue issue = this.comment.issue();
        final SolidList<Smart> comments = new SolidList<>(
            new Reversed<>(
                new Smarts<Comment.Smart>(
                    issue.comments().iterate(new Date(0L))
                )
            )
        );
        final String self = issue.repo().github().users().self().login();
        int mine = 0;
        for (final Comment.Smart cmt : comments) {
            if (!cmt.author().login().equals(self)) {
                break;
            }
            ++mine;
        }
        if (mine < Answer.MAX) {
            issue.comments().post(this.msg(success, Logger.format(msg, args)));
        } else {
            Logger.error(
                this, "too many (%d) comments from %s already in %s#%d",
                mine, self, issue.repo().coordinates(), issue.number()
            );
        }
    }

    /**
     * Make a message to post.
     * @param success Is it a report about success?
     * @param text The text
     * @return Text to post
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private String msg(final boolean success, final String text) {
        final StringBuilder msg = new StringBuilder(Tv.HUNDRED);
        try {
            msg.append(
                String.format(
                    "> %s\n\n",
                    StringUtils.abbreviate(
                        this.comment.body().replaceAll("\\p{Space}", SPACE),
                        Tv.HUNDRED
                    )
                )
            );
            final Collection<String> logins = new TreeSet<>();
            logins.add(this.comment.author().login());
            if (!success) {
                logins.add(
                    new Issue.Smart(this.comment.issue()).author().login()
                );
            }
            msg.append(
                new JoinedText(
                    SPACE,
                    new Mapped<>(
                        login -> String.format(
                            "@%s", login.toLowerCase(Locale.ENGLISH)
                        ),
                        logins
                    )
                ).asString()
            );
            msg.append(' ').append(text);
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            msg.append(text);
        }
        return Xembler.escape(msg.toString());
    }

}
