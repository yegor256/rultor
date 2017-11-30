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
import com.jcabi.github.Repo;
import com.jcabi.github.RepoCommit;
import com.jcabi.github.RepoCommit.Smart;
import com.jcabi.github.Smarts;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.json.JsonObject;
import org.cactoos.list.SolidList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.cactoos.text.JoinedText;

/**
 * Log of commits.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.51
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
final class CommitsLog {

    /**
     * Maximum to show.
     */
    private static final int MAX = 20;

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Ctor.
     * @param rpo Repo
     */
    CommitsLog(final Repo rpo) {
        this.repo = rpo;
    }

    /**
     * Release body text.
     * @param prev Previous release date.
     * @param current Current release date.
     * @return Release body text.
     * @throws IOException In case of problem communicating with git.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public String build(final Date prev, final Date current)
        throws IOException {
        final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'", Locale.ENGLISH
        );
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Collection<String> lines = new LinkedList<>();
        final Map<String, String> params = new SolidMap<String, String>(
            new MapEntry<String, String>("since", format.format(prev)),
            new MapEntry<String, String>("until", format.format(current))
        );
        final SolidList<Smart> commits = new SolidList<>(
            new Smarts<RepoCommit.Smart>(
                this.repo.commits().iterate(params)
            )
        );
        int count = 0;
        for (final RepoCommit.Smart commit : commits) {
            if (count >= CommitsLog.MAX) {
                lines.add(
                    String.format(
                        " * and %d more...",
                        commits.size() - CommitsLog.MAX
                    )
                );
                break;
            }
            lines.add(CommitsLog.asText(commit));
            ++count;
        }
        return new JoinedText(
            "\n",
            lines
        ).asString();
    }

    /**
     * Convert commit to text.
     * @checkstyle LineLengthCheck (1 line)
     *  see <a href="https://developer.github.com/v3/repos/commits/#list-commits-on-a-repository">link</a>
     * @param commit The commit
     * @return Text
     * @throws IOException If fails
     * @checkstyle MultipleStringLiteralsCheck (50 lines)
     */
    private static String asText(final RepoCommit.Smart commit)
        throws IOException {
        final StringBuilder line = new StringBuilder(Tv.HUNDRED);
        final JsonObject json = commit.json();
        line.append(" * ").append(commit.sha());
        if (!json.isNull("author")) {
            final JsonObject author = json.getJsonObject("author");
            if (!author.isNull("login")) {
                line.append(" by @").append(
                    author.getString("login")
                );
            }
        }
        if (!json.getJsonObject("commit").isNull("message")) {
            line.append(": ").append(
                commit.message()
                    .replaceAll("[\\p{Cntrl}\\p{Space}]+", " ")
                    .replaceAll("(?<=^.{30}).+$", "...")
            );
        }
        return line.toString();
    }

}
