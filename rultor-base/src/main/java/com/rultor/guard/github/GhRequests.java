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
import com.jcabi.log.Logger;
import com.rultor.guard.MergeRequest;
import com.rultor.guard.MergeRequests;
import java.io.IOException;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;

/**
 * Pull requests.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
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
     * Public ctor.
     * @param user User name
     * @param password Password
     * @param rep Repository name in Github
     */
    public GhRequests(
        @NotNull(message = "user name can't be NULL") final String user,
        @NotNull(message = "password can't be NULL") final String password,
        @NotNull(message = "repository can't be NULL") final String rep) {
        this(new Github.Simple(user, password), new Github.Repo(rep));
    }

    /**
     * Public ctor.
     * @param ghub Github
     * @param rep Repository name
     */
    protected GhRequests(final Github ghub, final Github.Repo rep) {
        this.github = ghub;
        this.repository = rep;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public Iterator<MergeRequest> iterator() {
        final GitHubClient client = this.github.client();
        final PullRequestService svc = new PullRequestService(client);
        try {
            final Iterator<PullRequest> iterator = svc.getPullRequests(
                this.repository,
                "open"
            ).iterator();
            return new Iterator<MergeRequest>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }
                @Override
                public MergeRequest next() {
                    return new GhRequest(
                        GhRequests.this.github,
                        GhRequests.this.repository,
                        iterator.next()
                    );
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s in %s",
            this.repository,
            this.github
        );
    }

}
