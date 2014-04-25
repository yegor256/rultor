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
package com.rultor.board;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.guard.github.Github;
import com.rultor.snapshot.Radar;
import com.rultor.snapshot.XemblyException;
import com.rultor.spi.Tag;
import com.rultor.spi.Tags;
import java.io.IOException;
import java.net.URI;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.xembly.SyntaxException;

/**
 * Posting a comment to Github.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
public final class GithubComment implements Billboard {

    /**
     * Github client.
     */
    private final transient Github github;

    /**
     * Public ctor.
     * @param login Github login
     * @param pwd Github password
     */
    public GithubComment(
        @NotNull(message = "login can't be NULL") final String login,
        @NotNull(message = "password can't be NULL") final String pwd) {
        this.github = new Github.Simple(login, pwd);
    }

    @Override
    public void announce(final boolean success) throws IOException {
        final Tags tags;
        try {
            tags = new Tags.Simple(new Radar().snapshot().tags());
        } catch (final SyntaxException ex) {
            throw new IOException(ex);
        } catch (final XemblyException ex) {
            throw new IOException(ex);
        }
        // @checkstyle MultipleStringLiterals (10 lines)
        if (tags.contains("git") && tags.contains("commit")) {
            final Tag git = tags.get("git");
            final URI uri = URI.create(git.attributes().get("url"));
            if ("github.com".equals(uri.getHost())) {
                this.post(
                    new Github.Repo(uri),
                    tags.get("commit").attributes().get("name"),
                    success
                );
            }
        }
    }

    /**
     * Post a comment for commit.
     * @param repo Github repository name
     * @param commit Commit name
     * @param success TRUE if success
     * @throws IOException If fails
     */
    private void post(final Github.Repo repo, final String commit,
        final boolean success) throws IOException {
        final GitHubClient client = this.github.client();
        final CommitService svc = new CommitService(client);
        final CommitComment comment = new CommitComment();
        if (success) {
            comment.setBody("This commit was built successfully");
        } else {
            comment.setBody("We failed to build this commit");
        }
        svc.addComment(repo, commit, comment);
    }

}
