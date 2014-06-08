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
package com.rultor.guard.github.jcabi;

import com.jcabi.github.Pull;
import com.jcabi.github.Repo;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link com.rultor.guard.github.jcabi.Approval}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class ApprovalTest {
    /**
     * Should always disapprove a request.
     * @throws IOException In case of problem.
     */
    @Test
    public void alwaysDisapprovesRequests() throws IOException {
        final Pull request = Mockito.mock(Pull.class);
        final Github client = Mockito.mock(Github.class);
        final Repo repo = Mockito.mock(Repo.class);
        MatcherAssert.assertThat(
            new Approval.Never().has(request, client, repo),
            Matchers.is(false)
        );
    }

    /**
     * Should always approve a request.
     * @throws IOException In case of problem.
     */
    @Test
    public void alwaysApprovesRequests() throws IOException {
        final Pull request = Mockito.mock(Pull.class);
        final Github client = Mockito.mock(Github.class);
        final Repo repo = Mockito.mock(Repo.class);
        MatcherAssert.assertThat(
            new Approval.Always().has(request, client, repo),
            Matchers.is(true)
        );
    }

    /**
     * Should revers given approval.
     * @throws IOException In case of problem.
     */
    @Test
    public void reversesApprovalForRequests() throws IOException {
        final Pull request = Mockito.mock(Pull.class);
        final Github client = Mockito.mock(Github.class);
        final Repo repo = Mockito.mock(Repo.class);
        MatcherAssert.assertThat(
            new Approval.Not(new Approval.Always()).has(request, client, repo),
            Matchers.is(false)
        );
        MatcherAssert.assertThat(
            new Approval.Not(new Approval.Never()).has(request, client, repo),
            Matchers.is(true)
        );
    }

    /**
     * Should approve only when all subrequests approved.
     * @throws IOException In case of problem.
     */
    @Test
    public void approvesIfSubRequestApproved() throws IOException {
        final Pull request = Mockito.mock(Pull.class);
        final Github client = Mockito.mock(Github.class);
        final Repo repo = Mockito.mock(Repo.class);
        MatcherAssert.assertThat(
            new Approval.And(
                new Approval.Always(), new Approval.Never()
            ).has(request, client, repo),
            Matchers.is(false)
        );
        MatcherAssert.assertThat(
            new Approval.And(
                new Approval.Never(), new Approval.Always()
            ).has(request, client, repo),
            Matchers.is(false)
        );
        MatcherAssert.assertThat(
            new Approval.And(
                new Approval.Always(), new Approval.Always()
            ).has(request, client, repo),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Approval.And(
                new Approval.Never(), new Approval.Never()
            ).has(request, client, repo),
            Matchers.is(false)
        );
    }

    /**
     * Should approve when any subrequests approved.
     * @throws IOException In case of problem.
     */
    @Test
    public void approvesIfAnySubRequestApproved() throws
        IOException {
        final Pull request = Mockito.mock(Pull.class);
        final Github client = Mockito.mock(Github.class);
        final Repo repo = Mockito.mock(Repo.class);
        MatcherAssert.assertThat(
            new Approval.Or(
                new Approval.Always(), new Approval.Never()
            ).has(request, client, repo),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Approval.Or(
                new Approval.Never(), new Approval.Always()
            ).has(request, client, repo),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Approval.Or(
                new Approval.Always(), new Approval.Always()
            ).has(request, client, repo),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Approval.Or(
                new Approval.Never(), new Approval.Never()
            ).has(request, client, repo),
            Matchers.is(false)
        );
    }
}
