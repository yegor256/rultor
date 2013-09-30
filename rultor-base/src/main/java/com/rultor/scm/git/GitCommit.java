/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.scm.git;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.scm.Commit;
import com.rultor.snapshot.TagLine;
import com.rultor.tools.Time;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Git Commit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "hash", "when", "who" })
@Loggable(Loggable.DEBUG)
final class GitCommit implements Commit {

    /**
     * Pattern for every log line.
     */
    private static final Pattern LINE = Pattern.compile(
        // @checkstyle LineLength (1 line)
        "([a-f0-9]{40}) ([\\w\\-@\\.]+) (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} (\\+|\\-)\\d{4}) (.*)"
    );

    /**
     * Name of commit.
     */
    private final transient String hash;

    /**
     * Date of commit.
     */
    private final transient Time when;

    /**
     * Author of commit.
     */
    private final transient String who;

    /**
     * Public ctor.
     * @param name Name of it
     * @param date When it happened
     * @param author Author of commit
     */
    protected GitCommit(final String name, final Time date,
        final String author) {
        this.hash = name;
        this.when = date;
        this.who = author;
        new TagLine("commit")
            .attr("name", name)
            .attr("date", date.toString())
            .attr("author", author)
            .log();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() throws IOException {
        return this.hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Time time() throws IOException {
        return this.when;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String author() throws IOException {
        return this.who;
    }

    /**
     * Convert git log line to commit.
     * @param line The line to convert
     * @return Commit
     */
    public static Commit parse(final String line) {
        final Matcher matcher = GitCommit.LINE.matcher(line);
        Validate.isTrue(matcher.matches(), "invalid line from Git: %s", line);
        final SimpleDateFormat fmt = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss X", Locale.ENGLISH
        );
        try {
            return new GitCommit(
                matcher.group(1),
                new Time(fmt.parse(matcher.group(Tv.THREE))),
                matcher.group(2)
            );
        } catch (ParseException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "failed to parse date `%s`",
                    matcher.group(Tv.THREE)
                ),
                ex
            );
        }
    }

}
