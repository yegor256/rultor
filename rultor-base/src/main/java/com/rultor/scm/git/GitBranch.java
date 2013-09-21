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
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.shell.Terminal;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Git.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "terminal", "dir", "label" })
@Loggable(Loggable.DEBUG)
public final class GitBranch implements Branch {

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
    private final transient String label;

    /**
     * Public ctor.
     * @param term Terminal to use for checkout
     * @param folder Directory with data
     * @param branch Name of the branch
     */
    public GitBranch(@NotNull(message = "terminal can't be NULL")
        final Terminal term, @NotNull(message = "folder can't be NULL")
        final String folder, @NotNull(message = "branch can't be NULL")
        final String branch) {
        this.terminal = term;
        this.dir = folder;
        this.label = branch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RetryOnFailure(verbose = false)
    @Loggable(value = Loggable.DEBUG, limit = Tv.FIVE)
    public Iterable<Commit> log() throws IOException {
        final String backslash = "\n";
        // @checkstyle AnonInnerLengthCheck (44 lines)
        return new Iterable<Commit>() {
            @Override
            public Iterator<Commit> iterator() {
                // @checkstyle AnonInnerLengthCheck (40 lines)
                return new Iterator<Commit>() {
                    private String hash;
                    // @checkstyle LineLength (1 line)
                    private List<String> lines = Arrays.asList(GitBranch.this.invokeGit("").split(backslash));
                    private Iterator<String> itr = lines.iterator();
                    @Override
                    public boolean hasNext() {
                        boolean has = false;
                        if (itr.hasNext()) {
                            has = true;
                        } else {
                            try {
                                // @checkstyle LineLength (1 line)
                                hash = GitCommit.parse(lines.get(lines.size() - 1)).name();
                                // @checkstyle LineLength (1 line)
                                lines = Arrays.asList(GitBranch.this.invokeGit("").split(backslash));
                                if (lines.size() == 0 || lines.size() == 1) {
                                    has = false;
                                } else {
                                    itr = lines.iterator();
                                    itr.next();
                                    has = itr.hasNext();
                                }
                            } catch (IOException ioe) {
                                throw new IllegalStateException(ioe);
                            }
                        }
                        return has;
                    }
                    @Override
                    public Commit next() {
                        return GitCommit.parse(itr.next());
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
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.label;
    }

    /**
     * Git command fetch next 10 commit log.
     * @param hash Commit hash.
     * @return String
     */
    private String invokeGit(final String hash) {
        String stdout;
        try {
            stdout = this.terminal.exec(
                new StringBuilder()
                    .append("DIR=`pwd`/")
                    .append(Terminal.quotate(Terminal.escape(this.dir)))
                    .append(" && cd \"$DIR/repo\"")
                    .append(" && GIT_SSH=\"$DIR/git-ssh.sh\"")
                    // @checkstyle LineLength (1 line)
                    .append(" && git log --pretty=format:'%H %ae %cd %s' --date=iso8601 -11 ")
                    .append(hash)
                    .toString()
            );
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        Logger.info(this, "Git log in branch `%s` retrieved", this.label);
        return stdout;
    }
}
