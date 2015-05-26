/**
 * Copyright (c) 2009-2015, rultor.com
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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Repo;
import com.jcabi.github.RepoCommit;
import com.jcabi.github.Smarts;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import javax.json.JsonObject;

/**
 * Log of commits.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.51
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
final class CommitsLog {

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
    public String build(final Date prev, final Date current)
        throws IOException {
        final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'", Locale.ENGLISH
        );
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Collection<String> lines = new LinkedList<>();
        final ImmutableMap<String, String> params =
            new ImmutableMap.Builder<String, String>()
                .put("since", format.format(prev))
                .put("until", format.format(current))
                .build();
        final Iterable<RepoCommit.Smart> commits = new Smarts<>(
            this.repo.commits().iterate(params)
        );
        int count = 0;
        final StringBuilder line = new StringBuilder(0);
        for (final RepoCommit.Smart commit : commits) {
            if (count > Tv.TWENTY) {
                lines.add(" * and more...");
                break;
            }
            final JsonObject json = commit.json();
            line.setLength(0);
            line.append(" * ").append(commit.sha());
            line.append(" by @").append(
                json.getJsonObject("author").getString("login")
            );
            if (!json.getJsonObject("commit").isNull("message")) {
                line.append(": ").append(
                    commit.message()
                        .replaceAll("[\\p{Cntrl}\\p{Space}]+", " ")
                        .replaceAll("(?<=^.{30}).+$", "...")
                );
            }
            lines.add(line.toString());
            ++count;
        }
        return Joiner.on('\n').join(lines);
    }

}
