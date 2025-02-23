/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import co.stateful.Locks;
import co.stateful.mock.MkSttc;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnAlone}.
 *
 * @since 1.6.1
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class QnAloneTest {

    /**
     * QnAlone can create lock repo.
     * @throws Exception In case of error.
     */
    @Test
    void locksRepo() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("deploy");
        final Talk talk = new Talk.InFile();
        final Locks locks = new MkSttc().locks();
        MatcherAssert.assertThat(
            "Deploy request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnAlone(talk, locks, new QnDeploy()).understand(
                        new Comment.Smart(issue.comments().get(1)), new URI("#")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/request[type='deploy']"
            )
        );
    }

}
