/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.github.Repo;
import com.jcabi.http.Request;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Access of @rultor to the repository that owns Rultor assets.
 *
 * <p>Before reading assets from another repository, Rultor makes sure
 * it can actually see it. A genuine access failure (HTTP 404 or 403)
 * is a configuration problem and is reported as
 * {@link Profile.ConfigException}. Any other unexpected status, e.g. a
 * 5xx answer from GitHub, is most likely a transient failure and stays
 * an {@link IOException}, so it is not reported as a configuration
 * problem the user has to fix.</p>
 *
 * @since 2.1
 */
final class AssetRepoAccess {

    /**
     * The repository with assets.
     */
    private final Repo repo;

    /**
     * Ctor.
     *
     * @param rpo Repository with assets
     */
    AssetRepoAccess(final Repo rpo) {
        this.repo = rpo;
    }

    /**
     * Makes sure the repository is accessible, a configuration failure
     * explains the access problem otherwise.
     *
     * @throws IOException If the repository is not accessible to
     *  @rultor, or GitHub answered with an unexpected status
     */
    void accessible() throws IOException {
        final int status = this.repo.github().entry().uri()
            .path("/repos")
            .path(this.repo.coordinates().user())
            .path(this.repo.coordinates().repo())
            .back()
            .method(Request.GET)
            .fetch()
            .status();
        if (status == HttpURLConnection.HTTP_NOT_FOUND
            || status == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new Profile.ConfigException(
                String.format(
                    "Can't access %s, make sure @rultor has read access to it, see https://doc.rultor.com/reference.html#assets",
                    this.repo.coordinates()
                )
            );
        }
        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException(
                String.format(
                    "GitHub answered with unexpected HTTP status %d when checking access to %s",
                    status, this.repo.coordinates()
                )
            );
        }
    }
}
