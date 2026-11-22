/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.github.Coordinates;
import com.jcabi.github.GitHub;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.RtGitHub;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Profile;
import jakarta.json.Json;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
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
 */
final class GithubProfileTest {

    /**
     * GithubProfile can fetch a YAML config with merge and asset entries.
     *
     * @throws Exception In case of error.
     */
    @Test
    void fetchesYamlConfigWithMergeAndAssetEntries() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            GithubProfileTest.yamlWithMergeAndAssets()
        );
        GithubProfileTest.addFriendConfig(repo);
        MatcherAssert.assertThat(
            "Profile should have all info",
            new GithubProfile(repo).read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='merge']/entry[@key='script']",
                "/p/entry[@key='assets']/entry[@key='test.xml']",
                "/p/entry[@key='assets']/entry[@key='beta']"
            )
        );
    }

    /**
     * GithubProfile can fetch a YAML config with an architect list.
     *
     * @throws Exception In case of error.
     */
    @Test
    void fetchesYamlConfigWithArchitectList() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            GithubProfileTest.yamlWithMergeAndAssets()
        );
        GithubProfileTest.addFriendConfig(repo);
        MatcherAssert.assertThat(
            "Architect should be saved",
            new GithubProfile(repo).read().xpath(
                "/p/entry[@key='architect']/item/text()"
            ),
            Matchers.contains("jeff", "donald")
        );
    }

    /**
     * GithubProfile can fetch a YAML config with a resolved asset entry.
     *
     * @throws Exception In case of error.
     */
    @Test
    void fetchesYamlConfigWithAssetEntry() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            GithubProfileTest.yamlWithMergeAndAssets()
        );
        GithubProfileTest.addFriendConfig(repo);
        MatcherAssert.assertThat(
            "Asset should be saved",
            new GithubProfile(repo).assets(),
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
                GithubProfileTest.repo(
                    new Joined(
                        System.lineSeparator(), "&*(fds:[[", "fd", ""
                    ).asString()
                )
            ).read()
        );
    }

    /**
     * GithubProfile can throw if asset is misconfigured.
     *
     * @throws Exception In case of error.
     * @since 1.33
     */
    @Test
    void throwsWhenAssetIsMisconfigured() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                System.lineSeparator(),
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
     *
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetsUsernameContainsUnderscore() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                System.lineSeparator(),
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
     *
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetsUsernameStartsWithUnderscore() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                System.lineSeparator(),
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
     *
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
            new GithubProfile(repo).assets().entrySet(),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    /**
     * GithubProfile can throw when rultor.yml is absent.
     *
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenRultorConfigIsAbsent() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                System.lineSeparator(),
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
     *
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenFriendNotDefined() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                System.lineSeparator(),
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
     *
     * @throws Exception In case of error.
     */
    @Test
    void throwsWhenAssetFileIsMissing() throws Exception {
        final Repo repo = GithubProfileTest.repo(
            new Joined(
                System.lineSeparator(),
                "assets:",
                "  test.xml: jeff/test1#test.xmls",
                "merge:",
                "  script: hello!"
            ).asString()
        );
        GithubProfileTest.addFriendConfig(repo);
        Assertions.assertThrows(
            Profile.ConfigException.class,
            new GithubProfile(repo)::assets
        );
    }

    /**
     * GithubProfile explains an inaccessible asset repository.
     *
     * <p>When the asset repository is private and @rultor cannot access
     * it, the error must say so, instead of claiming that the config
     * file is absent, see
     * https://github.com/yegor256/rultor/issues/1280.</p>
     *
     * @throws Exception In case of error.
     */
    @Test
    void explainsInaccessibleAssetRepository() throws Exception {
        final String cfg = GithubProfileTest.contentJson(
            String.format(
                "assets:%n  test.xml: jeff/test1#test.xml%n"
            )
        );
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, "{\"default_branch\":\"master\"}"))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_FOUND, "{}"))
            .start();
        try (container) {
            MatcherAssert.assertThat(
                "Error should explain the access problem",
                GithubProfileTest.assetFailure(
                    container, Profile.ConfigException.class
                ).getMessage(),
                Matchers.allOf(
                    Matchers.containsString("Can't access jeff/test1"),
                    Matchers.not(Matchers.containsString("must be present"))
                )
            );
        }
    }

    /**
     * GithubProfile keeps reporting a missing config of an accessible
     * repository.
     *
     * @throws Exception In case of error.
     */
    @Test
    void reportsMissingConfigWhenAssetRepoIsAccessible() throws Exception {
        final String cfg = GithubProfileTest.contentJson(
            String.format(
                "assets:%n  test.xml: jeff/test1#test.xml%n"
            )
        );
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, "{\"default_branch\":\"master\"}"))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, "{}"))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_FOUND, "{}"))
            .start();
        try (container) {
            MatcherAssert.assertThat(
                "Missing config should be reported",
                GithubProfileTest.assetFailure(
                    container, Profile.ConfigException.class
                ).getMessage(),
                Matchers.containsString(
                    ".rultor.yml file must be present in root directory of jeff/test1"
                )
            );
        }
    }

    /**
     * GithubProfile does not turn a transient GitHub failure into a
     * configuration failure.
     *
     * <p>A server-side status like 5xx is not an access problem of the
     * asset repository: the error must stay an I/O error, instead of
     * telling the user to grant read access, see the review of
     * https://github.com/yegor256/rultor/pull/2528.</p>
     *
     * @throws Exception In case of error.
     */
    @Test
    void keepsTransientHttpFailureAsIoException() throws Exception {
        final String cfg = GithubProfileTest.contentJson(
            String.format(
                "assets:%n  test.xml: jeff/test1#test.xml%n"
            )
        );
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, "{\"default_branch\":\"master\"}"))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, cfg))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_BAD_GATEWAY, "{}"))
            .start();
        try (container) {
            MatcherAssert.assertThat(
                "Transient GitHub failure must not be reported as an access problem",
                GithubProfileTest.assetFailure(container, IOException.class)
                    .getMessage(),
                Matchers.not(Matchers.containsString("Can't access"))
            );
        }
    }

    private static Exception assetFailure(final MkContainer container,
        final Class<? extends Exception> type) {
        return Assertions.assertThrows(
            type,
            () -> new GithubProfile(
                new RtGitHub(container.home()).repos().get(
                    new Coordinates.Simple("jeff/test2")
                ),
                "master"
            ).assets(),
            "Assets must fail with the expected exception type"
        );
    }

    private static String contentJson(final String text) {
        return String.format(
            "{\"path\":\".rultor.yml\",\"encoding\":\"base64\",\"content\":\"%s\"}",
            Base64.getEncoder().encodeToString(
                text.getBytes(StandardCharsets.UTF_8)
            )
        );
    }

    private static String yamlWithMergeAndAssets() throws Exception {
        return new Joined(
            System.lineSeparator(),
            "assets:",
            "  test.xml: jeff/test1#test.xml",
            "  beta: jeff/test1#test.xml",
            "architect:",
            " - jeff",
            " - donald",
            "merge:",
            "  script: hello!"
        ).asString();
    }

    private static void addFriendConfig(final Repo repo) throws IOException {
        repo.github()
            .repos()
            .get(new Coordinates.Simple("jeff/test1"))
            .contents().create(
                Json.createObjectBuilder()
                    .add("path", ".rultor.yml")
                    .add("message", "rultor config").add(
                        "content",
                        Base64.getEncoder().encodeToString(
                            String.format(
                                "friends:%s  - jeff/test2",
                                System.lineSeparator()
                            ).getBytes(StandardCharsets.UTF_8)
                        )
                    )
                    .build()
            );
    }

    private static Repo repo(final String yaml) throws IOException {
        final GitHub github = new MkGitHub("jeff");
        github.repos()
            .create(new Repos.RepoCreate("test1", false))
            .contents().create(
                Json.createObjectBuilder()
                    .add("path", "test.xml")
                    .add("message", "just test msg").add(
                        "content",
                        Base64.getEncoder().encodeToString(
                            "hey".getBytes(StandardCharsets.UTF_8)
                        )
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
                    Base64.getEncoder().encodeToString(
                        yaml.getBytes(StandardCharsets.UTF_8)
                    )
                )
                .build()
        );
        return repo;
    }
}
