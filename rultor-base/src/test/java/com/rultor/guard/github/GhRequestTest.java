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

import com.rultor.guard.MergeRequest;
import com.rultor.snapshot.Snapshot;
import java.net.HttpURLConnection;
import java.util.Date;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link GhRequest}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class GhRequestTest {

    /**
     * GhRequest can close request on success.
     * @throws Exception If some problem inside
     */
    @Test
    public void closesPullRequestOnSuccess() throws Exception {
        final Github github = Mockito.mock(Github.class);
        final GitHubClient client = Mockito.mock(GitHubClient.class);
        Mockito.doReturn(
            new GitHubResponse(
                Mockito.mock(HttpURLConnection.class),
                new Issue()
            )
        ).when(client).get(Mockito.any(GitHubRequest.class));
        Mockito.doReturn(client).when(github).client();
        final PullRequest req = new PullRequest();
        final PullRequestMarker marker = new PullRequestMarker();
        marker.setRef("");
        final User user = new User();
        user.setLogin("user");
        marker.setUser(user);
        final Repository repo = new Repository();
        repo.setName("test");
        marker.setRepo(repo);
        req.setBase(marker);
        req.setHead(marker);
        req.setCreatedAt(new Date());
        req.setTitle("some new pull request");
        final MergeRequest request = new GhRequest(
            github, new Github.Repo("test/test"), req
        );
        request.accept(new Snapshot("ADD 'test';"));
    }

}
