/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Issue;
import com.jcabi.github.Release;
import com.jcabi.github.Releases;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link CommentsTag}.
 *
 * @since 1.41.1
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class CommentsTagTest {

    /**
     * CommentsTag can create a release.
     * @throws Exception In case of error.
     */
    @Test
    void createsRelease() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(
            repo.github()
        );
        final String tag = "v1.0";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Release should be created",
            new Releases.Smart(repo.releases()).exists(tag),
            Matchers.is(true)
        );
    }

    /**
     * CommentsTag can duplicate a release.
     * @throws Exception In case of error.
     */
    @Test
    void duplicatesRelease() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(repo.github());
        final String tag = "v5.0";
        repo.releases().create(tag);
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Release should be created",
            new Releases.Smart(repo.releases()).exists(tag),
            Matchers.is(true)
        );
    }

    /**
     * CommentsTag can create a proper release message.
     * @throws Exception In case of error.
     */
    @Test
    void createsReleaseMessage() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(repo.github());
        final String tag = "v1.5";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        final Release.Smart smart = new Release.Smart(
            new Releases.Smart(repo.releases()).find(tag)
        );
        final String body = smart.body();
        MatcherAssert.assertThat(
            "Release message should be posted",
            body,
            Matchers.allOf(
                Matchers.containsString("Released by Rultor"),
                Matchers.containsString(
                    "see [build log](https://www.rultor.com/t/1-abcdef)"
                )
            )
        );
    }

    /**
     * CommentsTag can create a proper release title.
     * @throws Exception In case of error.
     */
    @Test
    void createsReleaseTitleFromIssue() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("Issue title", "");
        final Agent agent = new CommentsTag(repo.github());
        final String tag = "v1.6";
        agent.execute(CommentsTagTest.talk(issue, tag));
        MatcherAssert.assertThat(
            "Issue should be in release title",
            new Release.Smart(
                new Releases.Smart(repo.releases()).find(tag)
            ).name(),
            Matchers.equalTo("Issue title")
        );
    }

    /**
     * CommentsTag can create a proper release title.
     * @throws Exception In case of error.
     */
    @Test
    void createsReleaseTitleFromTalk() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("Title from issue", "");
        final Agent agent = new CommentsTag(repo.github());
        final String tag = "v1.7";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        final String title = "Custom Title";
        talk.modify(
            new Directives().xpath("/talk/request/args")
                .add("arg")
                .attr("name", "title")
                .set(title)
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "Release title should be from the command",
            new Release.Smart(
                new Releases.Smart(repo.releases()).find(tag)
            ).name(),
            Matchers.equalTo(title)
        );
    }

    /**
     * CommentsTag can create latest release if profile specify 'pre: false'.
     * @throws IOException In case of error.
     */
    @Test
    void createsLatestRelease() throws IOException {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues()
            .create(
                "Latest Release",
                "This issue is created for latest release"
            );
        final Agent agent = new CommentsTag(
            repo.github(),
            new Profile.Fixed(
                "<p>",
                "<entry key='release'>",
                "<entry key='pre'>",
                "true",
                "</entry></entry></p>"
            )
        );
        final String tag = "v1.1.latest";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        talk.modify(
            new Directives().xpath("/talk/request/args")
                .add("arg")
                .attr("name", "pre")
                .set("false")
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "We expect latest release to be created (not pre)",
            new Release.Smart(
                new Releases.Smart(repo.releases()).find(tag)
            ).prerelease(),
            Matchers.is(false)
        );
    }

    /**
     * CommentsTag can create latest release if profile specify 'pre: true',
     * but 'pre: false' is written in the comment.
     * @throws IOException In case of error.
     */
    @Test
    void createsLatestReleaseFromTalk() throws IOException {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues()
            .create(
                "Latest Release",
                "This issue is created for latest release"
            );
        final Agent agent = new CommentsTag(
            repo.github(),
            new Profile.Fixed(
                "<p>",
                "<entry key='release'>",
                "<entry key='pre'>",
                "false",
                "</entry></entry></p>"
            )
        );
        final String tag = "v1.1.latest";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        MatcherAssert.assertThat(
            "We expect latest release to be created (not pre)",
            new Release.Smart(
                new Releases.Smart(repo.releases()).find(tag)
            ).prerelease(),
            Matchers.is(false)
        );
    }

    /**
     * CommentsTag can create pre-release by default.
     * Check the default behaviour if profile specifies anything.
     * @throws IOException In case of error.
     */
    @Test
    void createsPreReleaseByDefault() throws IOException {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues()
            .create(
                "Pre Release",
                "This issue is created for pre release"
            );
        final Agent agent = new CommentsTag(repo.github());
        final String tag = "v1.1.pre-release";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        MatcherAssert.assertThat(
            "We expect pre-release to be created by default",
            new Release.Smart(
                new Releases.Smart(repo.releases()).find(tag)
            ).prerelease(),
            Matchers.is(true)
        );
    }

    /**
     * Make a talk with this tag.
     * @param issue The issue
     * @param tag The tag
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(final Issue issue, final String tag)
        throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .attr("later", "true")
                .add("wire")
                .add("href").set("http://test2").up()
                .add("github-repo").set(issue.repo().coordinates().toString())
                .up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .up()
                .add("request").attr("id", "abcdef")
                .add("author").set("yegor256").up()
                .add("type").set("release").up()
                .add("success").set("true").up()
                .add("args")
                .add("arg").attr("name", "tag").set(tag).up()
        );
        return talk;
    }
}
