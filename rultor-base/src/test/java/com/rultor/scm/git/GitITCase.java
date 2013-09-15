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

import com.google.common.io.Files;
import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.scm.SCM;
import com.rultor.shell.ShellMocker;
import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link Git}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class GitITCase {

    /**
     * URL of test Git repository.
     */
    private static final String URL =
        System.getProperty("failsafe.git.url");

    /**
     * Tag of test Git repository.
     */
    private static final String TAG =
        System.getProperty("failsafe.git.tag");

    /**
     * Branch of test Git repository.
     */
    private static final String BRANCH =
        System.getProperty("failsafe.git.branch");

    /**
     * Git can checkout a branch.
     * @throws Exception If some problem inside
     */
    @Test
    public void checksOutBranch() throws Exception {
        Assume.assumeNotNull(GitITCase.URL);
        final File dir = Files.createTempDir();
        final SCM git = new Git(
            new ShellMocker.Bash(dir),
            GitITCase.URL,
            "test"
        );
        final Branch branch = git.checkout("master");
        final Commit head = branch.log().iterator().next();
        MatcherAssert.assertThat(
            head.name().matches("[a-f0-9]{40}"),
            Matchers.is(true)
        );
    }

    /**
     * Git can list branches.
     * @throws Exception If some problem inside
     */
    @Test
    public void listsBranches() throws Exception {
        Assume.assumeNotNull(GitITCase.URL);
        final File dir = Files.createTempDir();
        final SCM git = new Git(
            new ShellMocker.Bash(dir),
            GitITCase.URL,
            "boom"
        );
        MatcherAssert.assertThat(
            git.branches(),
            Matchers.hasItems("HEAD", GitITCase.TAG)
        );
    }

    /**
     * Git can fetch a tag.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesTag() throws Exception {
        Assume.assumeNotNull(GitITCase.URL);
        final File dir = Files.createTempDir();
        final SCM git = new Git(
            new ShellMocker.Bash(dir),
            GitITCase.URL,
            "foo-4"
        );
        git.checkout(GitITCase.TAG);
    }

    /**
     * Git can fetch a branch.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesBranch() throws Exception {
        Assume.assumeNotNull(GitITCase.URL);
        final File dir = Files.createTempDir();
        final SCM git = new Git(
            new ShellMocker.Bash(dir),
            GitITCase.URL,
            "foo-ff"
        );
        git.checkout(GitITCase.BRANCH);
    }

}
