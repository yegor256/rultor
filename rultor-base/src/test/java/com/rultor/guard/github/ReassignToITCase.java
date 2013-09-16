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
package com.rultor.guard.github;

import org.eclipse.egit.github.core.PullRequest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link ReassignTo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class ReassignToITCase {

    /**
     * Github user.
     */
    private static final String USER =
        System.getProperty("failsafe.github.user");

    /**
     * Github password.
     */
    private static final String PASSWORD =
        System.getProperty("failsafe.github.password");

    /**
     * Github repository name.
     */
    private static final String REPO =
        System.getProperty("failsafe.github.repo");

    /**
     * ReassignTo can assign an issue to someone else.
     * @throws Exception If some problem inside
     */
    @Test
    public void reassignsIssueToSomeoneElse() throws Exception {
        Assume.assumeNotNull(ReassignToITCase.USER);
        final PullRequest request = new PullRequest();
        request.setId(1);
        MatcherAssert.assertThat(
            new ReassignTo(ReassignToITCase.USER).has(
                request,
                new Github.Simple(
                    ReassignToITCase.USER,
                    ReassignToITCase.PASSWORD
                ),
                new Github.Repo(ReassignToITCase.REPO)
            ),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new ReassignTo("").has(
                request,
                new Github.Simple(
                    ReassignToITCase.USER,
                    ReassignToITCase.PASSWORD
                ),
                new Github.Repo(ReassignToITCase.REPO)
            ),
            Matchers.is(true)
        );
    }

}
