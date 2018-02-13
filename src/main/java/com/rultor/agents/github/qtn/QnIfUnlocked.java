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
import com.jcabi.github.Contents;
import com.jcabi.github.Issue;
import com.jcabi.github.Pull;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;

/**
 * If target branch is unlocked.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.53
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnIfUnlocked implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Message bundle.
     */
    private static final String PATH = ".rultor.lock";

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnIfUnlocked(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final Issue issue = comment.issue();
        final Pull pull = issue.repo().pulls().get(issue.number());
        final JsonObject base = pull.json().getJsonObject("base");
        final String branch = base.getString("ref");
        final Collection<String> guards = QnIfUnlocked.guards(pull, branch);
        if (guards.isEmpty() || guards.contains(comment.author().login())) {
            req = this.origin.understand(comment, home);
        } else {
            new Answer(comment).post(
                false,
                QnIfUnlocked.PHRASES.getString("QnIfUnlocked.denied"),
                branch,
                new JoinedText(
                    ", ",
                    new Mapped<>(
                        input -> String.format("@%s", input),
                        guards
                    )
                ).asString()
            );
            req = Req.EMPTY;
        }
        return req;
    }

    /**
     * Is it allowed to merge?
     * @param pull The pull
     * @param branch The branch
     * @return TRUE if allowed
     * @throws IOException If fails
     */
    private static Collection<String> guards(final Pull pull,
        final String branch) throws IOException {
        final Contents contents = pull.repo().contents();
        final Collection<String> guards = new LinkedHashSet<>(0);
        if (contents.exists(QnIfUnlocked.PATH, branch)) {
            guards.addAll(
                Arrays.asList(
                    IOUtils.toString(
                        contents.get(QnIfUnlocked.PATH, branch).raw(),
                        CharEncoding.UTF_8
                    ).split("\n")
                )
            );
        }
        return guards;
    }

}
