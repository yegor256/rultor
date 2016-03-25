/**
 * Copyright (c) 2009-2016, rultor.com
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
package com.rultor.agents.github.qtn;

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for ${@link ReleaseTag}.
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.62
 */
public final class ReleaseTagTest {

    /**
     * ReleaseTag can deny release for outdated, semantically correct versions.
     * It does however allow any version number, if it contains anything
     * other than digits and dots.
     * @throws Exception In case of error
     */
    @Test
    public void validatesReleaseVersion() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        repo.releases().create("1.74");
        MatcherAssert.assertThat(
            new ReleaseTag(repo, "1.87.15").allowed(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new ReleaseTag(repo, "1.5-bar").allowed(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new ReleaseTag(repo, "1.9-beta").allowed(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new ReleaseTag(repo, "1.62").allowed(),
            Matchers.is(false)
        );
    }

    /**
     * ReleaseTag can retrieve the latest release version in the repo.
     * @throws Exception In case of error
     */
    @Test
    public void getsReferenceVersion() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final String latest = "2.2.1";
        repo.releases().create("1.0");
        repo.releases().create(latest);
        repo.releases().create("3.0-beta");
        MatcherAssert.assertThat(
            new ReleaseTag(repo, "2.4").reference(),
            Matchers.is(latest)
        );
    }
}
