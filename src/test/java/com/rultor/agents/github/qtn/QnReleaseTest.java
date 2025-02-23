/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnRelease}.
 *
 * @since 1.6
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class QnReleaseTest {

    /**
     * QnRelease can build a request.
     * @throws Exception In case of error
     */
    @Test
    void buildsRequest() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("release");
        MatcherAssert.assertThat(
            "Release request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnRelease().understand(
                        new Comment.Smart(issue.comments().get(1)), new URI("#")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/request[type='release']",
                "/request/args[count(arg) = 2]",
                "/request/args/arg[@name='head']",
                "/request/args/arg[@name='head_branch']"
            )
        );
    }

    /**
     * QnRelease can build a release request when the requested version is newer
     * than the last release.
     * @throws Exception In case of error
     */
    @Test
    void allowsNewerTag() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        repo.releases().create("1.5");
        issue.comments().post("release, title `1.7`");
        MatcherAssert.assertThat(
            "Release request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnRelease().understand(
                        new Comment.Smart(issue.comments().get(1)), new URI("#")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/request[type='release']",
                "/request/args[count(arg) = 2]",
                "/request/args/arg[@name='head']",
                "/request/args/arg[@name='head_branch']"
            )
        );
    }

    /**
     * QnRelease can deny release when tag is outdated.
     * @throws Exception In case of error
     */
    @Test
    void denyOutdatedTag() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        repo.releases().create("1.7");
        issue.comments().post("release `1.6`");
        MatcherAssert.assertThat(
            "Empty request should be created",
            new QnRelease().understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.EMPTY)
        );
        MatcherAssert.assertThat(
            " Comment about existing release should be posted",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString("There is already a release `1.7`")
        );
    }

    /**
     * QnRelease can accept release title.
     * @throws Exception In case of error
     */
    @Test
    void allowsToSetReleaseTitle() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("release `1.8`, title `Version 1.8.0`");
        MatcherAssert.assertThat(
            "Request should be created",
            new Xembler(
                new Directives().add("request").append(
                    new QnRelease().understand(
                        new Comment.Smart(issue.comments().get(1)), new URI("#")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/request[type='release']",
                "/request/args[count(arg) = 2]",
                "/request/args/arg[@name='head']",
                "/request/args/arg[@name='head_branch']"
            )
        );
    }

}
