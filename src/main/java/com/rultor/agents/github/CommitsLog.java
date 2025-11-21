/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import jakarta.json.JsonObject;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;

/**
 * Log of commits.
 *
 * @since 1.51
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
    @SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.UseDiamondOperator"})
    public String build(final Date prev, final Date current)
        throws IOException {
        final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'", Locale.ENGLISH
        );
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Collection<String> lines = new LinkedList<>();
        // @checkstyle DiamondOperatorCheck (2 lines)
        final Map<String, String> params = new MapOf<String, String>(
            new MapEntry<>("since", format.format(prev)),
            new MapEntry<>("until", format.format(current))
        );
        final List<Smart> commits = new ListOf<>(
            new Smarts<>(
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
        return new UncheckedText(
            new Joined(
                "\n",
                lines
            )
        ).asString();
    }

    // @checkstyle LineLengthCheck (4 line)
    /**
     * Convert commit to text.
     *  see <a href="https://developer.github.com/v3/repos/commits/#list-commits-on-a-repository">link</a>
     * @param commit The commit
     * @return Text
     * @throws IOException If fails
     * @checkstyle MultipleStringLiteralsCheck (50 lines)
     */
    private static String asText(final RepoCommit.Smart commit)
        throws IOException {
        final StringBuilder line = new StringBuilder(100);
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
