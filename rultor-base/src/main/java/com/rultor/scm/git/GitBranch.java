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
import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.shell.Terminal;
import com.rultor.spi.Signal;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * Git.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "terminal", "name" })
@Loggable(Loggable.DEBUG)
public final class GitBranch implements Branch {

    /**
     * Pattern for every log line.
     */
    private static final Pattern LINE = Pattern.compile(
        // @checkstyle LineLength (1 line)
        "([a-f0-9]{40}) ([\\w\\-@\\.]+) (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}) (.*)"
    );

    /**
     * Terminal to use.
     */
    private final transient Terminal terminal;

    /**
     * Directory.
     */
    private final transient String dir;

    /**
     * Branch name.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param term Terminal to use for checkout
     * @param folder Directory with data
     * @param branch Name of the branch
     */
    public GitBranch(@NotNull final Terminal term, @NotNull final String folder,
        @NotNull final String branch) {
        this.terminal = term;
        this.dir = folder;
        this.name = branch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Git branch '%s' at %s in %s",
            this.name, this.dir, this.terminal
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Commit> log() throws IOException {
        final String stdout = this.terminal.exec(
            new StringBuilder()
                .append("DIR=`pwd`/")
                .append(Terminal.escape(this.dir))
                .append(" && cd \"$DIR/repo\"")
                .append(" && GIT_SSH=\"$DIR/git-ssh.sh\"")
                // @checkstyle LineLength (1 line)
                .append(" && git log --pretty=format:'%H %ae %cd %s' --date=iso8601")
                .toString()
        );
        Signal.log(
            Signal.Mnemo.SUCCESS,
            "Git log in branch %s retrieved",
            this.name
        );
        final Iterable<String> lines = Arrays.asList(stdout.split("\n"));
        return new Iterable<Commit>() {
            @Override
            public Iterator<Commit> iterator() {
                final Iterator<String> iterator = lines.iterator();
                return new Iterator<Commit>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    @Override
                    public Commit next() {
                        return GitBranch.toCommit(iterator.next());
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Convert one log line to commit.
     * @param line The line to convert
     * @return Commit
     */
    private static Commit toCommit(final String line) {
        final Matcher matcher = GitBranch.LINE.matcher(line);
        Validate.isTrue(matcher.matches(), "invalid line from Git: %s", line);
        final SimpleDateFormat fmt = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss X", Locale.ENGLISH
        );
        try {
            return new Commit.Simple(
                matcher.group(1),
                fmt.parse(matcher.group(Tv.THREE)),
                matcher.group(2)
            );
        } catch (ParseException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "failed to parse date '%s'",
                    matcher.group(Tv.THREE)
                ),
                ex
            );
        }
    }

}
