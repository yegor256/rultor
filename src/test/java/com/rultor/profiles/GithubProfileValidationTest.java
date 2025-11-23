/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.github.GitHub;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.spi.Profile;
import jakarta.json.Json;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link GithubProfile} YAML validation.
 *
 * @since 1.1
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings(
    {
    "PMD.AvoidDuplicateLiterals",
    "PMD.TooManyMethods",
    "PMD.UseConcurrentHashMap"
    }
)
final class GithubProfileValidationTest {
    /**
     * GithubProfile will accept empty rultor configuration.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsEmptyYaml() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo("");
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can reject YAML with missing script in merge command.
     * @throws Exception In case of error.
     */
    @Disabled
    @Test
    void rejectsYamlWithoutMergeScript() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "merge:",
                "  - pwd"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).read()
        );
    }

    /**
     * GithubProfile can reject YAML with missing script in deploy command.
     * @throws Exception In case of error.
     */
    @Disabled
    @Test
    void rejectsYamlWithoutDeployScript() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "deploy:",
                "  - pwd"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).read()
        );
    }

    /**
     * GithubProfile can reject YAML with missing script in release command.
     * @throws Exception In case of error.
     */
    @Disabled
    @Test
    void rejectsYamlWithoutReleaseScript() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "release:",
                "  - pwd"
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).read()
        );
    }

    /**
     * GithubProfile can accept YAML with script in merge command.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsYamlWithOnlyMerge() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "merge:",
                " script:",
                "  - pwd"
            ).asString()
        );
        Assertions.assertDoesNotThrow(
            () -> new GithubProfile(repo).read()
        );
    }

    /**
     * GithubProfile can accept YAML with script in release command.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsYamlWithOnlyRelease() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "release:",
                " script:",
                "  - pwd"
            ).asString()
        );
        Assertions.assertDoesNotThrow(
            () -> new GithubProfile(repo).read()
        );
    }

    /**
     * GithubProfile can accept YAML with script in deploy command.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsYamlWithOnlyDeploy() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "deploy:",
                " script:",
                "  - pwd"
            ).asString()
        );
        Assertions.assertDoesNotThrow(
            () -> new GithubProfile(repo).read()
        );
    }

    /**
     * GithubProfile can accept YAML with script in all command.
     * @throws Exception In case of error.
     */
    @Test
    void acceptsYamlWithAllCommands() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "deploy:",
                " script:",
                "  - pwd",
                "release:",
                " script:",
                "  - pwd",
                "merge:",
                " script:",
                "  - pwd"
            ).asString()
        );
        Assertions.assertDoesNotThrow(
            () -> new GithubProfile(repo).read()
        );
    }

    /**
     * GithubProfile get the assets in the YAML.
     * @throws Exception In case of error.
     */
    @Test
    void getExistAssets() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "friends:",
                " - jeff/test",
                "assets:",
                " settings.xml: \"jeff/test#exist.txt\""
            ).asString()
        );
        final Map<String, InputStream> map = new GithubProfile(repo).assets();
        MatcherAssert.assertThat(
            "Asset should be added from profile",
            map.keySet(),
            Matchers.iterableWithSize(1)
        );
    }

    /**
     * GithubProfile reject get assets over not exist file.
     * @throws Exception In case of error.
     */
    @Test
    void rejectGetAssetWithNotExistFile() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "friends:",
                " - jeff/test",
                "assets:",
                " settings.xml: \"jeff/test#something.txt\""
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile reject get assets with wrong repository.
     * @throws Exception In case of error.
     */
    @Test
    void rejectGetAssetWithWrongRepo() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "friends:",
                " - jeff/test",
                "assets:",
                " settings.xml: \"jeff/fail#exist.txt\""
            ).asString()
        );
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile reject get assets with no friend user.
     * @throws Exception In case of error.
     */
    @Test
    void rejectGetAssetWithNoFriendUser() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "friends:",
                " - zheus/test",
                "assets:",
                " settings.xml: \"jeff/test#exist.txt\""
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * GithubProfile reject get assets with no friends.
     * @throws Exception In case of error.
     */
    @Test
    void rejectGetAssetWithNoFriends() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new Joined(
                "\n",
                "assets:",
                " settings.xml: \"jeff/test#exist.txt\""
            ).asString()
        );
        Assertions.assertThrows(
            Profile.ConfigException.class,
            () -> new GithubProfile(repo).assets()
        );
    }

    /**
     * Create repo with .rultor.yml inside.
     * @param yaml Content of .rultor.yml file
     * @return Created repo.
     * @throws IOException In case of error.
     */
    private static Repo repo(final String yaml) throws IOException {
        final GitHub github = new MkGitHub("jeff");
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("test", false)
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
        repo.contents().create(
            Json.createObjectBuilder()
                .add("path", "exist.txt")
                .add("message", "file exist")
                .add("content", "")
                .build()
        );
        return repo;
    }
}
