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
import com.jcabi.log.Logger;
import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.shell.Terminal;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Git.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "terminal", "label" })
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
    public String toString() {
        return String.format(
            "Git branch `%s` at `%s` in %s",
            this.label, this.dir, this.terminal
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
        Logger.info(this, "Git log in branch `%s` retrieved", this.label);
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
                        return GitCommit.parse(iterator.next());
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

}
