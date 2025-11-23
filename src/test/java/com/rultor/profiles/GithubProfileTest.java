/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.github.Coordinates;
import com.jcabi.github.GitHub;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Profile;
import jakarta.json.Json;
import java.io.IOException;
import java.util.Base64;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link GithubProfile}.
 *
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class GithubProfileTest {
    /**
     * GithubProfile can fetch a YAML config.
     * @throws Exception In case of error.
     */
    @Test
    void fetchesYamlConfig() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                "\n",
                "assets:",
                "  test.xml: jeff/test1#test.xml",
                "  beta: jeff/test1#test.xml",
                "architect:",
                " - jeff",
                " - donald",
                "merge:",
                "  script: hello!"
            ).asString()
        );
        final String yaml = "friends:\n  - jeff/test2";
        repo.github()
            .repos()
            .get(new Coordinates.Simple("jeff/test1"))
            .contents().create(
                Json.createObjectBuilder()
                    .add("path", ".rultor.yml")
                    .add("message", "rultor config")
                    .add(
                        "content",
                        Base64.getEncoder().encodeToString(yaml.getBytes())
                    )
                    .build()
            );
        final Profile profile = new GithubProfile(repo);
        MatcherAssert.assertThat(
            "Profile should have all info",
            profile.read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='merge']/entry[@key='script']",
                "/p/entry[@key='assets']/entry[@key='test.xml']",
                "/p/entry[@key='assets']/entry[@key='beta']"
            )
        );
        MatcherAssert.assertThat(
            "Architect should be saved",
            profile.read().xpath("/p/entry[@key='architect']/item/text()"),
            Matchers.contains("jeff", "donald")
        );
        MatcherAssert.assertThat(
            "Asset should be saved",
            profile.assets(),
            Matchers.hasEntry(
                Matchers.equalTo("test.xml"),
                Matchers.notNullValue()
            )
        );
    }

    /**
     * GithubProfile can throw when YAML is broken.
     */
    @Test
    void throwsWhenYamlIsBroken() {
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(
                GithubProfileTest.repo("&*(fds:[[\nfd\n")
            ).read()
        );
    }

    /**
     * GithubProfile can throw if asset is misconfigured.
     * @throws Exception In case of error.
     * @since 1.33
     */
    @Test
    void throwsWhenAssetIsMisconfigured() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                "\n",
                "assets: ",
                "  something.xml: -invalid.user.name/test1#test.xml"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile can throw if asset contains username with underscore.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetsUsernameContainsUnderscore() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                "\n",
                "assets: ",
                "  something.xml: invalid_username/test1#test.xml"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile can throw if asset contains username starting with an
     * underscore.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetsUsernameStartsWithUnderscore()
        throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                "\n",
                "assets: ",
                "  something.xml: _invalidusername/test1#test.xml"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile can accept asset from repo name that contains a dot.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsAssetsFromDotRepo() throws Exception {
        final GitHub github = new MkGitHub("jeff");
        final String name = "te.st";
        final Repo repo = github.repos().create(
            new Repos.RepoCreate(name, false)
        );
        repo.contents().create(
            Json.createObjectBuilder()
                .add("path", ".rultor.yml")
                .add("message", "just test")
                .add(
                    "content",
                    Base64.getEncoder().encodeToString(
                        new Joined(
                            "\n",
                            "assets: ",
                            String.format(
                                "  something.xml: jeff/%s#.rultor.yml", name
                            ),
                            "friends:", String.format("  - jeff/%s", name)
                        ).asString().getBytes()
                    )
                ).build()
        );
        MatcherAssert.assertThat(
            "Assets should be created",
            new GithubProfile(repo).assets().entrySet(),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    /**
     * GithubProfile can throw when rultor.yml is absent.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenRultorConfigIsAbsent() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                "\n",
                "assets:   ",
                "  something.xml: jeff/test2#.rultor.yml"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile can throw when friend is not defined.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenFriendNotDefined() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                "\n",
                "assets:    ",
                "  a.xml: jeff/test1#test.xml"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile should throw a ConfigException if some asset file doesn't
     * exist.
     * @throws Exception In case of error.
     */
    @Test
    void testAssetNotFound() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                "\n",
                "assets:",
                "  test.xml: jeff/test1#test.xmls",
                "merge:",
                "  script: hello!"
            ).asString()
        );
        final String yaml = "friends:\n  - jeff/test2";
        repo.github()
            .repos()
            .get(new Coordinates.Simple("jeff/test1"))
            .contents().create(
                Json.createObjectBuilder()
                    .add("path", ".rultor.yml")
                    .add("message", "rultor config")
                    .add(
                        "content",
                        Base64.getEncoder().encodeToString(yaml.getBytes())
                    )
                    .build()
            );
        final Profile profile = new GithubProfile(repo);
        Assertions.assertThrows(
            Profile.ConfigException.class,
                profile::assets
        );
    }

    /**
     * Make a repo with YAML inside.
     * @param yaml YAML config
     * @return Repo
     * @throws IOException If fails
     */
    private static Repo repo(final String yaml) throws IOException {
        final GitHub github = new MkGitHub("jeff");
        github.repos()
            .create(new Repos.RepoCreate("test1", false))
            .contents()
            .create(
                Json.createObjectBuilder()
                    .add("path", "test.xml")
                    .add("message", "just test msg")
                    .add(
                        "content",
                        Base64.getEncoder().encodeToString("hey".getBytes())
                    )
                    .build()
            );
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("test2", false)
        );
        repo.contents().create(
            Json.createObjectBuilder()
                .add("path", ".rultor.yml")
                .add("message", "just test")
                .add(
                    "content",
                    Base64.getEncoder().encodeToString(yaml.getBytes())
                )
                .build()
        );
        return repo;
    }
}
