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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.rultor.scm.Branch;
import com.rultor.scm.SCM;
import com.rultor.shell.Shell;
import com.rultor.shell.Terminal;
import com.rultor.shell.ssh.PrivateKey;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.Tag;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Git.
 *
 * <p>It is assumed that BASH is installed inside that shell.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "terminal", "url", "key" })
@Loggable(Loggable.DEBUG)
public final class Git implements SCM {

    /**
     * Terminal to use.
     */
    private final transient Terminal terminal;

    /**
     * Git URL.
     */
    private final transient String url;

    /**
     * Directory to use in terminal.
     */
    private final transient String dir;

    /**
     * Private key to use.
     */
    private final transient PrivateKey key;

    /**
     * Public ctor.
     * @param shl Shell to use for checkout
     * @param addr URL of git repository
     * @param folder Directory to use for clone
     * @checkstyle ParameterNumber (5 lines)
     */
    public Git(
        @NotNull(message = "shell can't be NULL") final Shell shl,
        @NotNull(message = "URL can't be NULL") final URL addr,
        @NotNull(message = "folder can't be NULL") final String folder) {
        this(
            shl, addr, folder,
            new PrivateKey(
                // @checkstyle StringLiteralsConcatenation (50 lines)
                "-----BEGIN RSA PRIVATE KEY-----\n"
                + "000000000000000000000000000000\n"
                + "-----END RSA PRIVATE KEY-----"
            )
        );
    }

    /**
     * Public ctor.
     * @param shl Shell to use for checkout
     * @param addr URL of git repository
     * @param folder Directory to use for clone
     * @param priv Private key to use locally
     * @checkstyle ParameterNumber (5 lines)
     */
    public Git(final Shell shl, final URL addr, final String folder,
        final PrivateKey priv) {
        this.terminal = new Terminal(shl);
        this.url = addr.toString();
        this.dir = folder;
        this.key = priv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Git repository at `%s` cloned to `%s` at %s accessed through %s",
            this.url,
            this.dir,
            this.terminal,
            this.key
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Tag("git")
    @Step("Git branch `${args[0]}` checked out")
    @RetryOnFailure
    public Branch checkout(final String name) throws IOException {
        this.terminal.exec(
            new StringBuilder(this.reset())
                .append(" && BRANCH=")
                .append(Terminal.escape(name))
                // @checkstyle LineLength (2 lines)
                .append(" && if [ $(git rev-parse --abbrev-ref HEAD) != $BRANCH ]; then git checkout $BRANCH; fi")
                .append(" && if git for-each-ref refs/heads/$BRANCH | grep commit; then git pull; fi")
                .toString(),
            this.key.asText()
        );
        return new GitBranch(this.terminal, this.dir, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Tag("git")
    @Step("found ${result.size()} refs in Git")
    @RetryOnFailure
    public Collection<String> branches() throws IOException {
        return Collections2.transform(
            Arrays.asList(
                this.terminal.exec(
                    new StringBuilder(this.reset())
                        // @checkstyle LineLength (1 line)
                        .append(" && git for-each-ref --format='%(refname:short)' refs/remotes/origin refs/tags")
                        .toString(),
                    this.key.asText()
                ).split("\n")
            ),
            new Function<String, String>() {
                @Override
                public String apply(final String input) {
                    String name = input;
                    if (input.startsWith("origin/")) {
                        name = input.substring(Tv.SEVEN);
                    }
                    return name;
                }
            }
        );
    }

    /**
     * Start script, to clone the repo.
     * @return Script to start
     */
    private String reset() {
        return new StringBuilder()
            .append("DIR=$(pwd)/")
            .append(Terminal.escape(this.dir))
            .append(" && URL=")
            .append(Terminal.escape(this.url))
            .append(" && mkdir -p \"$DIR\"")
            .append(" && ( cat > \"$DIR/id_rsa\" )")
            // @checkstyle LineLength (1 line)
            .append(" && ( echo \"set -x && git -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i \\\"$DIR/id_rsa\\\" $@\" > \"$DIR/git-ssh.sh\" )")
            .append(" && GIT_SSH=$DIR/git-ssh.sh")
            // @checkstyle LineLength (1 line)
            .append(" && if [ ! -d $DIR/repo ]; then git clone $URL $DIR/repo; fi")
            .append(" && cd $DIR/repo")
            .append(" && git remote set-url origin $URL")
            .append(" && git remote update -p")
            .append(" && git fetch origin --prune --tags")
            .append(" && git reset --hard")
            .append(" && git clean -f -d")
            .toString();
    }

}
