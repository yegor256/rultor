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
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

/**
 * Explicitly expressed by a comment.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "people", "regex" })
@Loggable(Loggable.DEBUG)
public final class Explicit implements Approval {

    /**
     * List of people who can approve (Github logins).
     */
    private final transient Array<String> people;

    /**
     * Regular expression for the message.
     */
    private final transient String regex;

    /**
     * Public ctor.
     * @param login Login of a single user we should listen to
     * @param reg Regular expression
     */
    public Explicit(final String login, final String reg) {
        this(Arrays.asList(login), reg);
    }

    /**
     * Public ctor.
     * @param ppl User logins
     * @param reg Regular expression
     */
    public Explicit(
        @NotNull(message = "names can't be NULL") final Collection<String> ppl,
        @NotNull(message = "regex can't be NULL") final String reg) {
        this.people = new Array<String>(ppl);
        this.regex = reg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(final PullRequest request, final Github github,
        final Github.Repo repo) throws IOException {
        final Time latest = this.latest(request, github, repo);
        boolean found = false;
        final Collection<Comment> comments =
            this.comments(request, github, repo);
        final Pattern ptn = Pattern.compile(
            this.regex, Pattern.DOTALL | Pattern.MULTILINE
        );
        for (Comment comment : comments) {
            if (latest.date().compareTo(comment.getUpdatedAt()) > 0) {
                continue;
            }
            if (!ptn.matcher(comment.getBody()).matches()) {
                continue;
            }
            final String login = comment.getUser().getLogin();
            if (!this.people.contains(login)) {
                Logger.info(
                    this,
                    // @checkstyle LineLength (1 line)
                    "message from `%s` matches `%s`, but we're listening to %[list]s",
                    login, this.regex, this.people
                );
                continue;
            }
            Logger.info(
                this, "pull request #%d is approved by %s with \"%[text]s\"",
                request.getId(), login, comment.getBody()
            );
            found = true;
            break;
        }
        if (!found) {
            Logger.info(
                // @checkstyle LineLength (1 line)
                this, "pull request #%d is NOT approved by any of %[list]s (%d comments)",
                request.getId(), this.people, comments.size()
            );
        }
        return found;
    }

    /**
     * Get date of latest commit.
     * @param request The request
     * @param github Github client
     * @param repo Repository to check
     * @return Time of latest commit in this pull request
     * @throws IOException If fails
     */
    private Collection<Comment> comments(final PullRequest request,
        final Github github, final Github.Repo repo) throws IOException {
        final GitHubClient client = github.client();
        final IssueService issues = new IssueService(client);
        return issues.getComments(
            repo.user(), repo.repo(), request.getNumber()
        );
    }

    /**
     * Get date of latest commit.
     * @param request The request
     * @param github Github client
     * @param repo Repository to check
     * @return Time of latest commit in this pull request
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Time latest(final PullRequest request, final Github github,
        final Github.Repo repo) throws IOException {
        final GitHubClient client = github.client();
        final PullRequestService psvc = new PullRequestService(client);
        Time latest = new Time(0);
        for (RepositoryCommit commit
            : psvc.getCommits(repo, request.getNumber())) {
            final Time time = new Time(
                commit.getCommit().getCommitter().getDate()
            );
            if (time.compareTo(latest) > 0) {
                latest = time;
            }
        }
        return latest;
    }

}
