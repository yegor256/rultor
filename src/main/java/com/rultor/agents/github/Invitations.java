/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor.agents.github;

import com.jcabi.github.Github;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.json.JsonObject;

/**
 * GitHub invitations.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.62
 */
public final class Invitations implements SuperAgent {

    /**
     * Github client.
     */
    private final transient Github github;

    /**
     * Ctor.
     * @param ghb Github client
     */
    public Invitations(final Github ghb) {
        this.github = ghb;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        // @checkstyle MultipleStringLiteralsCheck (2 lines)
        final Request entry = this.github.entry().reset("Accept").header(
            "Accept", "application/vnd.github.swamp-thing-preview+json"
        );
        final Iterable<JsonObject> all = new RtPagination<>(
            entry.uri().path("/user/repository_invitations").back(),
            RtPagination.COPYING
        );
        for (final JsonObject json : all) {
            this.accept(
                entry, json.getInt("id"),
                json.getJsonObject("repository").getString("full_name")
            );
        }
    }

    /**
     * Accept one invitation.
     * @param entry Entry to use
     * @param invitation The invitation number
     * @param repo The repo name
     * @throws IOException If fails
     */
    private void accept(final Request entry, final int invitation,
        final String repo) throws IOException {
        try {
            entry.uri().path("/user/repository_invitations/")
                .path(Integer.toString(invitation)).back()
                .method(Request.PATCH)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NO_CONTENT);
            Logger.info(
                this, "Invitation #%d to %s accepted",
                invitation, repo
            );
        } catch (final AssertionError ex) {
            Logger.info(
                this, "Failed to accept invitation #%d in %s: %s",
                invitation, repo, ex.getLocalizedMessage()
            );
        }
    }
}
