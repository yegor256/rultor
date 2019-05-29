/**
 * Copyright (c) 2009-2019, Yegor Bugayenko
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
import com.jcabi.github.Content;
import com.jcabi.github.Contents;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import javax.json.Json;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Unlock branch.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.53
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnUnlock implements Question {

    /**
     * Message bundle.
     */
    private static final String PATH = ".rultor.lock";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final XML args = QnUnlock.args(comment, home);
        final String branch;
        if (args.nodes("//arg[@name='branch']").isEmpty()) {
            branch = "master";
        } else {
            branch = args.xpath("//arg[@name='branch']/text()").get(0);
        }
        final Contents contents = comment.issue().repo().contents();
        if (contents.exists(QnUnlock.PATH, branch)) {
            contents.remove(
                Json.createObjectBuilder()
                    .add("path", QnUnlock.PATH)
                    .add(
                        "sha",
                        new Content.Smart(
                            contents.get(QnUnlock.PATH, branch)
                        ).sha()
                    )
                    .add(
                        "message",
                        String.format(
                            "#%d branch \"%s\" unlocked, by request of @%s",
                            comment.issue().number(),
                            branch,
                            comment.author().login()
                        )
                    )
                    .add("branch", branch)
                    .build()
            );
            new Answer(comment).post(
                true,
                String.format(
                    QnUnlock.PHRASES.getString("QnUnlock.response"),
                    branch
                )
            );
        } else {
            new Answer(comment).post(
                false,
                String.format(
                    QnUnlock.PHRASES.getString("QnUnlock.does-not-exist"),
                    branch
                )
            );
        }
        Logger.info(this, "unlock request in #%d", comment.issue().number());
        return Req.DONE;
    }

    /**
     * Get args.
     * @param comment The comment
     * @param home Home
     * @return Args
     * @throws IOException If fails
     */
    private static XML args(final Comment.Smart comment, final URI home)
        throws IOException {
        return new XMLDocument(
            new Xembler(
                new Directives().add("args").up().append(
                    new QnParametrized(
                        (cmt, hme) -> () -> new Directives().xpath("/")
                    ).understand(comment, home).dirs()
                )
            ).xmlQuietly()
        );
    }

}
