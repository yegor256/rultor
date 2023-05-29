/**
 * Copyright (c) 2009-2023 Yegor Bugayenko
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
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link Dephantomizes}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.59.7
 */
public final class DephantomizesTest {

    /**
     * Dephantomizes can remove request and wire.
     * @throws IOException In case of error
     */
    @Test
    public void removesRequestAndWire() throws IOException {
        final MkGithub github = new MkGithub();
        final Repo repo = github.randomRepo();
        final Talk talk = DephantomizesTest.talk(repo, 0);
        new Dephantomizes(github).execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath("/talk[not(request) and not(wire)]")
        );
    }

    /**
     * Dephantomizes can remove request and wire.
     * @throws IOException In case of error
     */
    @Test
    public void doesntTouchRequestAndWire() throws IOException {
        final MkGithub github = new MkGithub();
        final Repo repo = github.randomRepo();
        repo.issues().create("title", "desc");
        final Talk talk = DephantomizesTest.talk(repo, 1);
        new Dephantomizes(github).execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath("/talk[request and wire]")
        );
    }

    /**
     * Make a fake talk.
     * @param repo Repo
     * @param issue The issue
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(final Repo repo, final int issue)
        throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("wire").add("href").set("#").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue)).up().up()
                .add("request").attr("id", "a1b2c3d4")
                .add("author").set("yegor256").up()
                .add("type").set("deploy").up()
                .add("args")
        );
        return talk;
    }

}
