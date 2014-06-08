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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.log.Logger;
import com.rultor.guard.MergeRequest;
import com.rultor.guard.MergeRequests;
import com.rultor.snapshot.Step;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;

/**
 * Pull requests.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @todo #445 Migrate this class to jcabi-github and move the logic to jcabi
 *  subpackage.
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "github", "repository" })
@Loggable(Loggable.DEBUG)
public final class GhRequests implements MergeRequests {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Repository name.
     */
    private final transient Github.Repo repository;

    /**
     * Approval to get.
     */
    private final transient Approval approval;

    /**
     * Public ctor.
     * @param user User name
     * @param password Password
     * @param rep Repository name in Github
     */
    public GhRequests(final String user, final String password,
        final String rep) {
        this(
            new Github.Simple(user, password), new Github.Repo(rep),
            new Approval.Always()
        );
    }

    /**
     * Public ctor.
     * @param user User name
     * @param password Password
     * @param rep Repository name in Github
     * @param appr Approval
     * @checkstyle ParameterNumber (7 lines)
     */
    public GhRequests(
        @NotNull(message = "user can't be NULL") final String user,
        @NotNull(message = "password can't be NULL") final String password,
        @NotNull(message = "repository can't be NULL") final String rep,
        @NotNull(message = "approval can't be NULL") final Approval appr) {
        this(new Github.Simple(user, password), new Github.Repo(rep), appr);
    }

    /**
     * Public ctor.
     * @param ghub Github
     * @param rep Repository name
     * @param appr Approval
     */
    protected GhRequests(
        @NotNull(message = "github can't be NULL") final Github ghub,
        @NotNull(message = "repo can't be NULL") final Github.Repo rep,
        @NotNull(message = "approval can't be NULL") final Approval appr) {
        this.github = ghub;
        this.repository = rep;
        this.approval = appr;
    }

    @Override
    public Iterator<MergeRequest> iterator() {
        try {
            final Iterator<PullRequest> requests =
                this.filter(this.fetch()).iterator();
            return new Iterator<MergeRequest>() {
                @Override
                public boolean hasNext() {
                    return requests.hasNext();
                }
                @Override
                public MergeRequest next() {
                    return new GhRequest(
                        GhRequests.this.github,
                        GhRequests.this.repository,
                        requests.next()
                    );
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Fetch all pull requests.
     * @return Collection of them
     * @throws IOException If fails
     */
    @RetryOnFailure(verbose = false)
    @Step("found ${result.size()} pull request(s) in Github")
    private Collection<PullRequest> fetch() throws IOException {
        final GitHubClient client = this.github.client();
        final PullRequestService svc = new PullRequestService(client);
        final Collection<PullRequest> requests =
            svc.getPullRequests(this.repository, "open");
        Logger.info(
            this,
            // @checkstyle LineLength (1 line)
            "%d request(s) found in Github, requestLimit=%d, remainingRequests=%d",
            requests.size(), client.getRequestLimit(),
            client.getRemainingRequests()
        );
        return requests;
    }

    /**
     * Filter out requests that were already seen.
     * @param list List of all of them
     * @return Collection of them
     * @throws IOException If fails
     */
    @Step("${result.size()} out of ${args[0].size} request(s) approved")
    private Collection<PullRequest> filter(final Collection<PullRequest> list)
        throws IOException {
        final Collection<PullRequest> requests = new LinkedList<PullRequest>();
        for (final PullRequest request : list) {
            if (this.approval.has(request, this.github, this.repository)) {
                requests.add(request);
            }
        }
        return requests;
    }

}
