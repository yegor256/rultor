/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link GithubProfile}.
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class GithubProfileTest {

    /**
     * GithubProfile can fetch a YAML config (entries).
     * @throws Exception In case of error.
     */
    @Test
    void fetchesYamlConfigEntries() throws Exception {
        MatcherAssert.assertThat(
            "Profile should have all info",
            GithubProfile.fromRepo(GithubProfileTest.yamlConfigRepo()).read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='merge']/entry[@key='script']",
                "/p/entry[@key='assets']/entry[@key='test.xml']",
                "/p/entry[@key='assets']/entry[@key='beta']"
            )
        );
    }

    /**
     * GithubProfile can fetch a YAML config (architect).
     * @throws Exception In case of error.
     */
    @Test
    void fetchesYamlConfigArchitect() throws Exception {
        MatcherAssert.assertThat(
            "Architect should be saved",
            GithubProfile.fromRepo(GithubProfileTest.yamlConfigRepo())
                .read().xpath("/p/entry[@key='architect']/item/text()"),
            Matchers.contains("jeff", "donald")
        );
    }

    /**
     * GithubProfile can fetch a YAML config (assets).
     * @throws Exception In case of error.
     */
    @Test
    void fetchesYamlConfigAssets() throws Exception {
        MatcherAssert.assertThat(
            "Asset should be saved",
            GithubProfile.fromRepo(GithubProfileTest.yamlConfigRepo()).assets(),
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
            () -> GithubProfile.fromRepo(
                GithubProfileTest.repo(String.format("&*(fds:[[%nfd%n"))
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
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> GithubProfile.fromRepo(
                GithubProfileTest.repo(
                    new Joined(
                        System.lineSeparator(),
                        "assets: ",
                        "  something.xml: -invalid.user.name/test1#test.xml"
                    ).asString()
                )
            ).assets()
        );
    }

    /**
     * GithubProfile can throw if asset contains username with underscore.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetsUsernameContainsUnderscore() throws Exception {
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> GithubProfile.fromRepo(
                GithubProfileTest.repo(
                    new Joined(
                        System.lineSeparator(),
                        "assets: ",
                        "  something.xml: invalid_username/test1#test.xml"
                    ).asString()
                )
            ).assets()
        );
    }

    /**
     * GithubProfile can throw if asset contains username starting with an
     * underscore.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetsUsernameStartsWithUnderscore() throws Exception {
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> GithubProfile.fromRepo(
                GithubProfileTest.repo(
                    new Joined(
                        System.lineSeparator(),
                        "assets: ",
                        "  something.xml: _invalidusername/test1#test.xml"
                    ).asString()
                )
            ).assets()
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
                .add("message", "just test").add(
                    "content",
                    Base64.getEncoder().encodeToString(
                        new Joined(
                            System.lineSeparator(),
                            "assets: ",
                            String.format(
                                "  something.xml: jeff/%s#.rultor.yml", name
                            ),
                            "friends:", String.format("  - jeff/%s", name)
                        ).asString().getBytes(StandardCharsets.UTF_8)
                    )
                ).build()
        );
        MatcherAssert.assertThat(
            "Assets should be created",
            GithubProfile.fromRepo(repo).assets().entrySet(),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    /**
     * GithubProfile can throw when rultor.yml is absent.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenRultorConfigIsAbsent() throws Exception {
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> GithubProfile.fromRepo(
                GithubProfileTest.repo(
                    new Joined(
                        System.lineSeparator(),
                        "assets:   ",
                        "  something.xml: jeff/test2#.rultor.yml"
                    ).asString()
                )
            ).assets()
        );
    }

    /**
     * GithubProfile can throw when friend is not defined.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenFriendNotDefined() throws Exception {
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> GithubProfile.fromRepo(
                GithubProfileTest.repo(
                    new Joined(
                        System.lineSeparator(),
                        "assets:    ",
                        "  a.xml: jeff/test1#test.xml"
                    ).asString()
                )
            ).assets()
        );
    }

    /**
     * GithubProfile should throw a ConfigException if some asset file doesn't
     * exist.
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetNotFound() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                System.lineSeparator(),
                "assets:",
                "  test.xml: jeff/test1#test.xmls",
                "merge:",
                "  script: hello!"
            ).asString()
        );
        repo.github()
            .repos()
            .get(new Coordinates.Simple("jeff/test1"))
            .contents().create(
                Json.createObjectBuilder()
                    .add("path", ".rultor.yml")
                    .add("message", "rultor config").add(
                        "content",
                        Base64.getEncoder().encodeToString(
                            String.format("friends:%n  - jeff/test2")
                                .getBytes(StandardCharsets.UTF_8)
                        )
                    )
                    .build()
            );
        Assertions.assertThrows(
            Profile.ConfigException.class,
                GithubProfile.fromRepo(repo)::assets
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
            .contents().create(
                Json.createObjectBuilder()
                    .add("path", "test.xml")
                    .add("message", "just test msg").add(
                        "content",
                        Base64.getEncoder().encodeToString("hey".getBytes(StandardCharsets.UTF_8))
                    )
                    .build()
            );
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("test2", false)
        );
        repo.contents().create(
            Json.createObjectBuilder()
                .add("path", ".rultor.yml")
                .add("message", "just test").add(
                    "content",
                    Base64.getEncoder().encodeToString(yaml.getBytes(StandardCharsets.UTF_8))
                )
                .build()
        );
        return repo;
    }
}
