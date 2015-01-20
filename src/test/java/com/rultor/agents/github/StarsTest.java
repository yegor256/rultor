/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.agents.github;

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Tests for {@link Stars}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class StarsTest {
    /**
     * Stars can star a new repo.
     * @throws java.io.IOException In case of error
     */
    @Test
    public void starsNewRepo() throws IOException {
        final MkGithub github = new MkGithub();
        final Repo repo = github.randomRepo();
        final Talk talk = this.talk(repo);
        new Stars(github).execute(talk);
        MatcherAssert.assertThat(
            repo.stars().starred(),
            Matchers.is(true)
        );
    }

    /**
     * Stars should leave already starred repo.
     * @throws java.io.IOException In case of error
     */
    @Test
    public void leavesStarredRepo() throws IOException {
        final MkGithub github = new MkGithub();
        final Repo repo = github.randomRepo();
        final Talk talk = this.talk(repo);
        repo.stars().star();
        new Stars(github).execute(talk);
        MatcherAssert.assertThat(
            repo.stars().starred(),
            Matchers.is(true)
        );
    }

    /**
     * Create a test talk.
     * @param repo Repo to use
     * @return Test Talk.
     * @throws IOException In case of error.
     */
    private Talk talk(final Repo repo) throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("wire").add("href").set("#").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
        );
        return talk;
    }
}
