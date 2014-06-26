/**
 * Copyright (c) 2009-2014, rultor.com
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
import java.net.URI;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubClient;

/**
 * Github.
 *
 * @author Yegor Bugayenko (yegor@woquo.com)
 * @version $Id$
 */
@Immutable
public interface Github {

    /**
     * Get Github client.
     * @return Client
     */
    GitHubClient client();

    /**
     * Simple repository ID provider.
     */
    @Loggable(Loggable.DEBUG)
    @ToString
    @EqualsAndHashCode(of = { "name", "login" })
    @Immutable
    final class Repo implements IRepositoryIdProvider {
        /**
         * User name.
         */
        private final transient String login;
        /**
         * Repo name.
         */
        private final transient String name;
        /**
         * Public ctor.
         * @param repo Repository name
         */
        public Repo(@NotNull final String repo) {
            final String[] parts = repo.split("/", 2);
            this.login = parts[0];
            this.name = parts[1];
        }
        /**
         * Public ctor.
         * @param uri URI of the github repo
         */
        public Repo(@NotNull final URI uri) {
            this(uri.getPath().replaceAll("^/|\\.git$", ""));
        }
        @Override
        public String generateId() {
            return String.format("%s/%s", this.login, this.name);
        }
        /**
         * Owner of the repo (user name).
         * @return User name
         */
        public String user() {
            return this.login;
        }
        /**
         * Repository name.
         * @return Repo name
         */
        public String repo() {
            return this.name;
        }
    }

    /**
     * Simple implementation.
     */
    @Loggable(Loggable.DEBUG)
    @ToString(exclude = "password")
    @EqualsAndHashCode(of = { "username", "password" })
    @Immutable
    final class Simple implements Github {
        /**
         * User name.
         */
        private final transient String username;
        /**
         * Password.
         */
        private final transient String password;
        /**
         * Public ctor.
         * @param user User name in Github
         * @param pwd Password of the user
         */
        public Simple(@NotNull final String user, @NotNull final String pwd) {
            this.username = user;
            this.password = pwd;
        }
        @Override
        public GitHubClient client() {
            final GitHubClient client = new GitHubClient();
            client.setUserAgent("www.rultor.com");
            client.setCredentials(this.username, this.password);
            return client;
        }
    }

}
