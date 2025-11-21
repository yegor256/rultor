/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnIfCollaborator}.
 *
 * @since 1.40.4
 */
final class QnIfCollaboratorTest {

    /**
     * QnCollaborators can block a request.
     * @throws Exception In case of error.
     */
    @Test
    void blocksRequest() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        repo.collaborators().add("friend");
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("deploy");
        MatcherAssert.assertThat(
            "Empty request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnIfCollaborator(new QnDeploy()).understand(
                        new Comment.Smart(issue.comments().get(1)), new URI("#")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPaths("/request[not(type)]")
        );
    }

}
