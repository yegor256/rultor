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
import com.jcabi.log.Logger;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;

/**
 * Release request.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3.6
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnRelease implements Question {

    /**
     * Pattern matching the version tag of the release enclosed by backticks.
     */
    private static final Pattern QUESTION_PATTERN = Pattern.compile("`(.+)`");

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final Issue issue = comment.issue();
        Logger.info(
            this, "release request found in %s#%d, comment #%d",
            issue.repo().coordinates(), issue.number(), comment.number()
        );
        final Req req;
        final Matcher matcher = QnRelease.QUESTION_PATTERN
            .matcher(comment.body());
        if (matcher.find()) {
            final String name = matcher.group(1);
            final ReleaseTag release = new ReleaseTag(issue.repo(), name);
            if (release.allowed()) {
                req = QnRelease.affirmative(comment, home);
            } else {
                new Answer(comment).post(
                    false,
                    String.format(
                        QnRelease.PHRASES.getString("QnRelease.invalid-tag"),
                        name,
                        release.reference()
                    )
                );
                req = Req.EMPTY;
            }
        } else {
            req = QnRelease.affirmative(comment, home);
        }
        return req;
    }

    /**
     * Confirms that Rultor is starting the release process.
     * @param comment Comment that triggered the release
     * @param home URI of the release tail
     * @return Req.Simple containing the release parameters
     * @throws IOException on error
     */
    private static Req affirmative(final Comment.Smart comment,
        final URI home) throws IOException {
        new Answer(comment).post(
            true,
            String.format(
                QnRelease.PHRASES.getString("QnRelease.start"),
                home.toASCIIString()
            )
        );
        return new Req.Simple(
            "release",
            new SolidMap<String, String>(
                new MapEntry<String, String>("head_branch", "master"),
                new MapEntry<String, String>(
                    "head",
                    String.format(
                        "git@github.com:%s.git",
                        comment.issue().repo().coordinates()
                    )
                )
            )
        );
    }

}
