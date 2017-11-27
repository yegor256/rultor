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
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.Mapped;

/**
 * Question asked by one of them.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "xpath", "origin" })
public final class QnAskedBy implements Question {

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
    public QnAskedBy(final Profile prof, final String path,
        final Question qtn) {
        this.profile = prof;
        this.xpath = path;
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Req req;
        final Collection<String> logins = this.commanders(
            comment.issue().repo()
        );
        if (logins.isEmpty() || logins.contains(comment.author().login())) {
            req = this.origin.understand(comment, home);
        } else {
            new Answer(comment).post(
                false,
                String.format(
                    QnAskedBy.PHRASES.getString("QnAskedBy.denied"),
                    this.commandersAsDelimitedList(
                        logins,
                        comment.issue().repo().github().users().self().login()
                    )
                )
            );
            req = Req.EMPTY;
        }
        return req;
    }

    /**
     * Format list of commanders with {@code @} prefix, comma-delimited.
     * @param logins Commanders
     * @param excluded Excluded commander
     * @return Comma-delimited names
     */
    private String commandersAsDelimitedList(final Collection<String> logins,
            final String excluded) {
        return StringUtils.join(
            new Mapped<>(
                input -> String.format("@%s", input),
                new Filtered<>(
                    login -> !excluded.equals(login),
                    logins
                )
            ),
            ", "
        );
    }

    /**
     * Get list of commanders.
     * @param repo Repo
     * @return Their logins
     * @throws IOException If fails
     */
    private Collection<String> commanders(final Repo repo) throws IOException {
        final Collection<String> logins = new LinkedList<>();
        final XML xml = this.profile.read();
        logins.addAll(new Crew(repo).names());
        logins.addAll(xml.xpath(this.xpath));
        logins.addAll(xml.xpath("/p/entry[@key='architect']/item/text()"));
        return logins;
    }

}
