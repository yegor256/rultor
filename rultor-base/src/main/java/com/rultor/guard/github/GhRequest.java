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
import com.rultor.guard.MergeRequest;
import com.rultor.scm.Branch;
import com.rultor.snapshot.Radar;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.TagLine;
import com.rultor.tools.Exceptions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

/**
 * Github pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "github", "repository", "issue", "src", "dest" })
@Loggable(Loggable.DEBUG)
final class GhRequest implements MergeRequest {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Repository.
     */
    private final transient Github.Repo repository;

    /**
     * Pull request issue ID.
     */
    private final transient int issue;

    /**
     * Source branch.
     */
    private final transient Branch src;

    /**
     * Destination branch.
     */
    private final transient Branch dest;

    /**
     * Public ctor.
     * @param ghub Github
     * @param rep Repository name
     * @param req Pull request from Github
     */
    GhRequest(final Github ghub, final Github.Repo rep,
        final PullRequest req) {
        this.github = ghub;
        this.repository = rep;
        this.src = new Branch.Passive(
            GhRequest.uri(req.getHead()),
            req.getHead().getRef()
        );
        this.dest = new Branch.Passive(
            GhRequest.uri(req.getBase()),
            req.getBase().getRef()
        );
        this.issue = req.getNumber();
        new TagLine("github")
            .attr("issue", Integer.toString(req.getNumber()))
            .attr("baseRef", req.getBase().getRef())
            .attr("baseRepo", req.getBase().getRepo().getName())
            .attr("baseUser", req.getBase().getUser().getLogin())
            .attr("headRef", req.getHead().getRef())
            .attr("headRepo", req.getHead().getRepo().getName())
            .attr("headUser", req.getHead().getUser().getLogin())
            .log();
    }

    @Override
    public String name() {
        return Integer.toString(this.issue);
    }

    @Override
    @Step("notified GitHub pull request ${this.issue} that merging started")
    public void started() throws IOException {
        final GitHubClient client = this.github.client();
        final IssueService issues = new IssueService(client);
        final InputStream xsl = this.getClass().getResourceAsStream(
            "github-started.xsl"
        );
        try {
            issues.createComment(
                this.repository, this.issue,
                new Radar().render(xsl)
            );
        } finally {
            xsl.close();
        }
    }

    @Override
    @Step("accepted GitHub pull request ${this.issue}")
    public void accept() throws IOException {
        final GitHubClient client = this.github.client();
        final IssueService issues = new IssueService(client);
        final InputStream xsl = this.getClass().getResourceAsStream(
            "github-accept.xsl"
        );
        try {
            issues.createComment(
                this.repository, this.issue,
                new Radar().render(xsl)
            );
        } finally {
            xsl.close();
        }
        try {
            this.merge();
        } catch (final RequestException ex) {
            issues.createComment(
                this.repository, this.issue,
                String.format(
                    "Failed to merge:\n\n```\n%s\n```",
                    Exceptions.stacktrace(ex)
                )
            );
            throw ex;
        }
    }

    @Override
    @Step("rejected GitHub pull request ${this.issue}")
    public void reject() throws IOException {
        final GitHubClient client = this.github.client();
        final IssueService svc = new IssueService(client);
        final InputStream xsl = this.getClass().getResourceAsStream(
            "github-reject.xsl"
        );
        try {
            svc.createComment(
                this.repository, this.issue,
                new Radar().render(xsl)
            );
        } finally {
            xsl.close();
        }
    }

    @Override
    public Branch source() {
        return this.src;
    }

    @Override
    public Branch destination() {
        return this.dest;
    }

    /**
     * Make an URI of Git repo.
     * @param head Marker
     * @return URI of the repo
     */
    private static URI uri(final PullRequestMarker head) {
        return URI.create(
            String.format(
                "ssh://git@github.com/%s/%s.git",
                head.getUser().getLogin(),
                head.getRepo().getName()
            )
        );
    }

    /**
     * Do the actual merging.
     * @throws IOException If fails
     */
    @RetryOnFailure(verbose = false)
    private void merge() throws IOException {
        final GitHubClient client = this.github.client();
        final PullRequestService svc = new PullRequestService(client);
        final IssueService issues = new IssueService(client);
        svc.merge(
            this.repository, this.issue,
            String.format(
                "#%d: pull request %s",
                this.issue,
                issues.getIssue(this.repository, this.issue).getTitle()
            )
        );
    }

}
