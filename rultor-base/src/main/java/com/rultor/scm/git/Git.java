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
import com.rultor.scm.Branch;
import com.rultor.scm.SCM;
import com.rultor.shell.Shell;
import com.rultor.shell.ssh.PrivateKey;
import java.io.IOException;
import java.net.URL;
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
@EqualsAndHashCode(of = { "shells", "url", "key" })
@Loggable(Loggable.DEBUG)
public final class Git implements SCM {

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Git URL.
     */
    private final transient String url;

    /**
     * Private key to use.
     */
    private final transient PrivateKey key;

    /**
     * Public ctor.
     * @param shl Shell to use for checkout
     * @param addr URL of git repository
     * @param priv Private key to use locally
     */
    public Git(@NotNull final Shell shl, @NotNull final URL addr,
        @NotNull final PrivateKey priv) {
        this.shell = shl;
        this.url = addr.toString();
        this.key = priv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Git repository at '%s' cloned to %s with %s",
            this.url,
            this.shell,
            this.key
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Branch checkout(final String name) throws IOException {
        this.shell.exec(name, null, null, null);
        return new GitBranch(this.shell, name);
    }

}
