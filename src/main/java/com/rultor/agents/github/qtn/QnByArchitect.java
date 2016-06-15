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
package com.rultor.agents.github.qtn;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.rultor.agents.github.AddressedMessage;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Question by architect only (if configured).
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.45
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "xpath", "origin" })
public final class QnByArchitect implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * XPath.
     */
    private final transient String xpath;

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param prof Profile
     * @param path XPath in profile with a list of logins
     * @param qtn Original question
     */
    public QnByArchitect(final Profile prof, final String path,
        final Question qtn) {
        this.profile = prof;
        this.xpath = path;
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final Issue.Smart issue = new Issue.Smart(comment.issue());
        final List<String> logins = Lists.transform(
            this.profile.read().xpath(this.xpath),
            new Function<String, String>() {
                @Override
                public String apply(final String input) {
                    return input.toLowerCase(Locale.ENGLISH);
                }
            }
        );
        final boolean legal = logins.isEmpty()
            || logins.contains(
                comment.author().login().toLowerCase(Locale.ENGLISH)
            )
            || logins.contains(
                issue.author().login().toLowerCase(Locale.ENGLISH)
            );
        if (legal) {
            req = this.origin.understand(comment, home);
        } else {
            new Answer(
                new AddressedMessage(
                    comment,
                    String.format(
                        QnByArchitect.PHRASES.getString(
                            "QnByArchitect.denied"
                        ),
                        logins.get(0).toLowerCase(Locale.ENGLISH)
                    ),
                    Arrays.asList(comment.author().login())
                )
            ).post();
            req = Req.DONE;
        }
        return req;
    }

}
