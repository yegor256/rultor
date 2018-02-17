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
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Pull;
import com.jcabi.github.PullRef;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;

/**
 * Merge request.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnMerge implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Issue.Smart issue = new Issue.Smart(comment.issue());
        final Req req;
        if (issue.isPull() && issue.isOpen()) {
            new Answer(comment).post(
                true,
                String.format(
                    QnMerge.PHRASES.getString("QnMerge.start"),
                    home.toASCIIString()
                )
            );
            Logger.info(
                this, "merge request found in %s#%d, comment #%d",
                issue.repo().coordinates(), issue.number(), comment.number()
            );
            req = QnMerge.pack(
                comment,
                issue.repo().pulls().get(issue.number())
            );
        } else {
            new Answer(comment).post(
                false,
                QnMerge.PHRASES.getString("QnMerge.already-closed")
            );
            req = Req.EMPTY;
        }
        return req;
    }

    /**
     * Pack a pull request.
     * @param comment The comment we're in
     * @param pull Pull
     * @return Req
     * @throws IOException If fails
     */
    private static Req pack(final Comment.Smart comment,
        final Pull pull) throws IOException {
        final PullRef head = pull.head();
        final PullRef base = pull.base();
        final Req req;
        final String repo = "repo";
        if (head.json().isNull(repo)) {
            new Answer(comment).post(
                false,
                QnMerge.PHRASES.getString("QnMerge.head-is-gone")
            );
            req = Req.EMPTY;
        } else if (base.json().isNull(repo)) {
            new Answer(comment).post(
                false,
                QnMerge.PHRASES.getString("QnMerge.base-is-gone")
            );
            req = Req.EMPTY;
        } else {
            req = new Req.Simple(
                "merge",
                new SolidMap<String, String>(
                    new MapEntry<String, String>(
                        "pull_id",
                        Integer.toString(pull.number())
                    ),
                    new MapEntry<String, String>(
                        "pull_title",
                        new Issue.Smart(comment.issue()).title()
                    ),
                    new MapEntry<String, String>(
                        "fork_branch",
                        head.ref()
                    ),
                    new MapEntry<String, String>(
                        "head_branch",
                        base.ref()
                    ),
                    new MapEntry<String, String>(
                        "head",
                        String.format(
                            "git@github.com:%s.git",
                            base.repo().coordinates().toString()
                        )
                    ),
                    new MapEntry<String, String>(
                        "fork",
                        String.format(
                            "git@github.com:%s.git",
                            head.repo().coordinates().toString()
                        )
                    )
                )
            );
        }
        return req;
    }

}
