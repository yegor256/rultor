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

import com.jcabi.github.Comment;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import org.cactoos.list.SolidList;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Question that passes github_issue as env variable.
 *
 * @author Andrej Istomin (andrej.istomin.ikeen@gmail.com)
 * @version $Id$
 * @since 2.0
 */
public final class QnGithubIssue implements Question {

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param question Original question
     */
    public QnGithubIssue(final Question question) {
        this.origin = question;
    }

    @Override
    public Req understand(final Comment.Smart comment, final URI home)
        throws IOException {
        Req req = this.origin.understand(comment, home);
        final SolidList<Directive> reqDirs = new SolidList<>(req.dirs());
        if (!reqDirs.isEmpty()) {
            final Directives dirs = new Directives().append(reqDirs);
            req = new Req() {
                @Override
                public Iterable<Directive> dirs() {
                    return dirs.addIf("args")
                        .add("arg").attr("name", "github_issue")
                        .set(String.valueOf(comment.issue().number()))
                        .up().up();
                }
            };
        }
        return req;
    }
}
