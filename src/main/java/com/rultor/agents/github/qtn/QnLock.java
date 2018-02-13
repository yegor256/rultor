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
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.json.Json;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Lock branch.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.53
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnLock implements Question {

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
        final XML args = QnLock.args(comment, home);
        final String branch;
        if (args.nodes("//arg[@name='branch']").isEmpty()) {
            branch = "master";
        } else {
            branch = args.xpath("//arg[@name='branch']/text()").get(0);
        }
        final Collection<String> users = new LinkedHashSet<>(0);
        users.add(comment.author().login().toLowerCase(Locale.ENGLISH));
        if (!args.nodes("//arg[@name='users']").isEmpty()) {
            users.addAll(
                new org.cactoos.list.Mapped<>(
                    input -> StringUtils.stripStart(
                        input.trim().toLowerCase(Locale.ENGLISH),
                        "@"
                    ),
                    Arrays.asList(
                        args.xpath(
                            "//arg[@name='users']/text()"
                        ).get(0).split(",")
                    )
                )
            );
        }
        final Contents contents = comment.issue().repo().contents();
        if (contents.exists(QnLock.PATH, branch)) {
            new Answer(comment).post(
                false,
                String.format(
                    QnLock.PHRASES.getString("QnLock.already-exists"),
                    branch
                )
            );
        } else {
            contents.create(
                Json.createObjectBuilder()
                    .add("path", QnLock.PATH)
                    .add(
                        "message",
                        String.format(
                            "#%d: branch \"%s\" locked by request of @%s",
                            comment.issue().number(),
                            branch,
                            comment.author().login()
                        )
                    )
                    .add(
                        "content",
                        Base64.encodeBase64String(
                            new JoinedText(
                                "\n",
                                users
                            ).asString().getBytes(CharEncoding.UTF_8)
                        )
                    )
                    .add("branch", branch)
                    .build()
            );
            new Answer(comment).post(
                true,
                String.format(
                    QnLock.PHRASES.getString("QnLock.response"),
                    branch,
                    new JoinedText(
                        ", ",
                        new Mapped<>(
                            input -> String.format("@%s", input),
                            users
                        )
                    ).asString()
                )
            );
        }
        Logger.info(this, "lock request in #%d", comment.issue().number());
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
                        new Question() {
                            @Override
                            public Req understand(final Comment.Smart cmt,
                                final URI hme) {
                                return new Req() {
                                    @Override
                                    public Iterable<Directive> dirs() {
                                        return new Directives().xpath("/");
                                    }
                                };
                            }
                        }
                    ).understand(comment, home).dirs()
                )
            ).xmlQuietly()
        );
    }

}
