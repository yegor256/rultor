/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.google.common.base.Joiner;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Profile;
import java.io.IOException;
import javax.json.Json;
import org.apache.commons.codec.binary.Base64;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for ${@link GithubProfile} YAML validation.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class GithubProfileValidationTest {
    /**
     * GithubProfile can reject empty YAML.
     * @throws Exception In case of error.
     */
    @Ignore
    @Test(expected = Profile.ConfigException.class)
    public void rejectsEmptyYaml() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo("");
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can reject YAML without release phase.
     * @throws Exception In case of error.
     */
    @Ignore
    @Test(expected = Profile.ConfigException.class)
    public void rejectsYamlWithoutRelease() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            Joiner.on('\n').join(
                "deploy:",
                "  script: whoami",
                "merge:",
                "  script: pwd"
            )
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can reject YAML without merge phase.
     * @throws Exception In case of error.
     */
    @Ignore
    @Test(expected = Profile.ConfigException.class)
    public void rejectsYamlWithoutMerge() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            Joiner.on('\n').join(
                "deploy:",
                "  script: whoami",
                "release:",
                "  script: pwd"
            )
        );
        new GithubProfile(repo).read();
    }

    /**
     * GithubProfile can reject YAML without deploy phase.
     * @throws Exception In case of error.
     */
    @Ignore
    @Test(expected = Profile.ConfigException.class)
    public void rejectsYamlWithoutDeploy() throws Exception {
        final Repo repo = GithubProfileValidationTest.repo(
            Joiner.on('\n').join(
                "merge:",
                "  script: whoami",
                "release:",
                "  script: pwd"
            )
        );
        new GithubProfile(repo).read();
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
            Json.createObjectBuilder().add("name", "test").build()
        );
        repo.contents().create(
            Json.createObjectBuilder()
                .add("path", ".rultor.yml")
                .add("message", "just test")
                .add("content", Base64.encodeBase64String(yaml.getBytes()))
                .build()
        );
        return repo;
    }
}
