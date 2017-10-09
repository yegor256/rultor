/**
 * Copyright (c) 2009-2017, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.profiles;

import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import javax.json.Json;
import org.cactoos.text.JoinedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for ${@link GithubProfile} YAML validation.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings
    (
        {
            "PMD.AvoidDuplicateLiterals",
            "PMD.TooManyMethods",
            "PMD.UseConcurrentHashMap"
        }
    )
public final class GithubProfileValidationTest {
    /**
     * GithubProfile will accept empty rultor configuration.
     * @throws Exception In case of error.
     */
    @Test
    public void acceptsEmptyYaml() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo("");
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can reject YAML with missing script in merge command.
     * @throws Exception In case of error.
     */
    @Ignore
    @Test(expected = Profile.ConfigException.class)
    public void rejectsYamlWithoutMergeScript() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "merge:",
                "  - pwd"
            ).asString()
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can reject YAML with missing script in deploy command.
     * @throws Exception In case of error.
     */
    @Ignore
    @Test(expected = Profile.ConfigException.class)
    public void rejectsYamlWithoutDeployScript() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "deploy:",
                "  - pwd"
            ).asString()
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can reject YAML with missing script in release command.
     * @throws Exception In case of error.
     */
    @Ignore
    @Test(expected = Profile.ConfigException.class)
    public void rejectsYamlWithoutReleaseScript() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "release:",
                "  - pwd"
            ).asString()
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can accept YAML with script in merge command.
     * @throws Exception In case of error.
     */
    @Test
    public void acceptsYamlWithOnlyMerge() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "merge:",
                " script:",
                "  - pwd"
            ).asString()
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can accept YAML with script in release command.
     * @throws Exception In case of error.
     */
    @Test
    public void acceptsYamlWithOnlyRelease() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "release:",
                " script:",
                "  - pwd"
            ).asString()
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can accept YAML with script in deploy command.
     * @throws Exception In case of error.
     */
    @Test
    public void acceptsYamlWithOnlyDeploy() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "deploy:",
                " script:",
                "  - pwd"
            ).asString()
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can accept YAML with script in all command.
     * @throws Exception In case of error.
     */
    @Test
    public void acceptsYamlWithAllCommands() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
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
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile get the assets in the YAML.
     * @throws Exception In case of error.
     */
    @Test
    public void getExistAssets() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "friends:",
                " - jeff/test",
                "assets:",
                " settings.xml: \"jeff/test#exist.txt\""
            ).asString()
        );
        final Map<String, InputStream> map = new GithubProfile(repo).assets();
        MatcherAssert.assertThat(
            map.keySet(),
            Matchers.<String>iterableWithSize(1)
        );
    }

    /**
     * GithubProfile reject get assets over not exist file.
     * @throws Exception In case of error.
     */
    @Test(expected = Profile.ConfigException.class)
    public void rejectGetAssetWithNotExistFile() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "friends:",
                " - jeff/test",
                "assets:",
                " settings.xml: \"jeff/test#something.txt\""
            ).asString()
        );
        new GithubProfile(repo).assets();
    }

    /**
     * GithubProfile reject get assets with wrong repository.
     * @throws Exception In case of error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectGetAssetWithWrongRepo() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "friends:",
                " - jeff/test",
                "assets:",
                " settings.xml: \"jeff/fail#exist.txt\""
            ).asString()
        );
        new GithubProfile(repo).assets();
    }

    /**
     * GithubProfile reject get assets with no friend user.
     * @throws Exception In case of error.
     */
    @Test(expected = Profile.ConfigException.class)
    public void rejectGetAssetWithNoFriendUser() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "friends:",
                " - zheus/test",
                "assets:",
                " settings.xml: \"jeff/test#exist.txt\""
            ).asString()
        );
        new GithubProfile(repo).assets();
    }

    /**
     * GithubProfile reject get assets with no friends.
     * @throws Exception In case of error.
     */
    @Test(expected = Profile.ConfigException.class)
    public void rejectGetAssetWithNoFriends() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            new JoinedText(
                "\n",
                "assets:",
                " settings.xml: \"jeff/test#exist.txt\""
            ).asString()
        );
        new GithubProfile(repo).assets();
    }

    /**
     * Create repo with .rultor.yml inside.
     * @param yaml Content of .rultor.yml file
     * @return Created repo.
     * @throws IOException In case of error.
     */
    private static Repo repo(final String yaml) throws IOException {
        final Github github = new MkGithub("jeff");
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
