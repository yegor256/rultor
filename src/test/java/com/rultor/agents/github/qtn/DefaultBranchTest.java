/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Coordinates;
import com.jcabi.github.RtGitHub;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link DefaultBranch}.
 *
 * @since 2.1
 */
final class DefaultBranchTest {

    @Test
    void reportsMissingRepoWhenGithubAnswersNotFound() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_FOUND, "{}"))
            .start();
        try (container) {
            MatcherAssert.assertThat(
                "Missing repo does not surface as RepoNotFoundException",
                Assertions.assertThrows(
                    RuntimeException.class,
                    () -> new DefaultBranch(
                        new RtGitHub(container.home()).repos().get(
                            new Coordinates.Simple("gone", "Gr4vE-yArd")
                        )
                    ).toString()
                ),
                Matchers.instanceOf(RepoNotFoundException.class)
            );
        }
    }
}
