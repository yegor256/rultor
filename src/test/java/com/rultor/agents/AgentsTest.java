/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor.agents;

import co.stateful.Sttc;
import co.stateful.mock.MkSttc;
import com.jcabi.github.Check;
import com.jcabi.github.Github;
import com.jcabi.github.Pull;
import com.jcabi.github.PullComments;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkChecks;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link Agents}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.7
 */
public final class AgentsTest {

    /**
     * Agents can process a talk.
     * @throws Exception In case of error.
     */
    @Test
    public void processesTalk() throws Exception {
        final Talk talk = new Talk.InFile();
        final Github github = new MkGithub();
        final Sttc sttc = new MkSttc();
        final Profile profile = new Profile.Fixed();
        new Agents(github, sttc).agent(talk, profile).execute(talk);
    }

    @Test
    public void doubleMessage() throws IOException {
        final MkGithub github = new MkGithub("hi");
        final Profile profile = new Profile.Fixed();
        final Repo repo = github.randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        final Pull pull = repo.pulls().create("", "head", "base");
        repo.issues().get(pull.number()).comments().post( "@rultor merge");

        final MkChecks checks = (MkChecks) pull.checks();
        checks.create(Check.Status.COMPLETED, Check.Conclusion.FAILURE);
        final Talk talk = AgentsTest.talk(pull);

        new Agents(github, new MkSttc()).agent(talk, profile).execute(talk);

        final PullComments comments = pull.comments();
        System.out.println(comments);
    }


    /**
     * Make talk from issue.
     * @param pull The issue
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(final Pull pull) throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .attr("later", "true")
                .add("wire")
                .add("href").set("http://test2").up()
                .add("github-repo").set(pull.repo().coordinates().toString())
                .up()
                .add("github-issue").set(Integer.toString(pull.number())).up()
        );
        return talk;
    }
}
