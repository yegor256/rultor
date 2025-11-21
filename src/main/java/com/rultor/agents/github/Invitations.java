/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.GitHub;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.net.HttpURLConnection;
import jakarta.json.JsonObject;

/**
 * GitHub invitations.
 *
 * @since 1.62
 */
public final class Invitations implements SuperAgent {

    /**
     * GitHub client.
     */
    private final transient GitHub github;

    /**
     * Ctor.
     * @param ghb GitHub client
     */
    public Invitations(final GitHub ghb) {
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
