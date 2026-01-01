/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.twitter;

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.UUID;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.Joined;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for {@link Tweets}.
 *
 * @since 1.30
 */
final class TweetsTest {

    /**
     * Tweets can post a tweet.
     * @throws Exception In case of error.
     */
    @Test
    void postsTweet() throws Exception {
        final Repo repo = new MkGitHub().repos().create(
            new Repos.RepoCreate(
                UUID.randomUUID().toString().replace("-", ""), false
            )
        );
        final Twitter twitter = Mockito.mock(Twitter.class);
        final Talk talk = TweetsTest.talk(repo, repo.issues().create("", ""));
        new Tweets(repo.github(), twitter).execute(talk);
        Mockito.verify(twitter).post(
            ArgumentMatchers.contains(repo.coordinates().repo())
        );
    }

    /**
     * Tweets can post a tweet with language tags.
     * @throws Exception In case of error.
     */
    @Test
    void postsTweetWithLanguages() throws Exception {
        final Repo repo = new MkGitHub().repos().create(
            new Repos.RepoCreate(
                UUID.randomUUID().toString().replace("-", ""), false
            )
        );
        final Twitter twitter = Mockito.mock(Twitter.class);
        new Tweets(repo.github(), twitter).execute(
            TweetsTest.talk(repo, repo.issues().create("", ""))
        );
        Mockito.verify(twitter).post(
            ArgumentMatchers.contains(
                new Joined(
                    " ",
                    new Mapped<>(
                        lang -> String.format("#%s", lang.name()),
                        repo.languages()
                    )
                ).asString()
            )
        );
    }

    /**
     * Creates a talk with repo and issue.
     * @param repo Repo to use
     * @param issue Issue to use
     * @return Created Talk
     * @throws IOException In case of error
     */
    private static Talk talk(final Repo repo, final Issue issue)
        throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("wire")
                .add("href").set("http://test").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .up()
                .add("request").attr("id", "1")
                .add("author").set("yegor256").up()
                .add("msec").set("1234567").up()
                .add("success").set("true").up()
                .add("type").set("release").up()
                .add("args").add("arg").attr("name", "tag").set("1.7")
        );
        return talk;
    }
}
