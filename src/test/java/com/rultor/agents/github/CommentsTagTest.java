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

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Release;
import com.jcabi.github.Releases;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link CommentsTag}.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.41.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class CommentsTagTest {

    /**
     * CommentsTag can create a release.
     * @throws Exception In case of error.
     */
    @Test
    public void createsRelease() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(
            repo.github()
        );
        final String tag = "1.0";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        MatcherAssert.assertThat(
            new Releases.Smart(repo.releases()).exists(tag),
            Matchers.is(true)
        );
    }

    /**
     * CommentsTag can duplicate a release.
     * @throws Exception In case of error.
     */
    @Test
    public void duplicatesRelease() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(repo.github());
        final String tag = "v5.0";
        repo.releases().create(tag);
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        MatcherAssert.assertThat(
            new Releases.Smart(repo.releases()).exists(tag),
            Matchers.is(true)
        );
    }

    /**
     * CommentsTag cannot release an older version.
     * @throws IOException In case of error.
     */
    @Test
    public void rejectsOldRelease() throws IOException {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(repo.github());
        repo.releases().create("1.0");
        repo.releases().create("2.0");
        repo.releases().create("3.0-b");
        final Talk talk = CommentsTagTest.talk(issue, "1.5");
        agent.execute(talk);
        final Comment.Smart response = new Comment.Smart(
            repo.issues().get(1).comments().get(1)
        );
        MatcherAssert.assertThat(
            response.body(),
            Matchers.containsString("version tag is too low")
        );
    }

    /**
     * CommentsTag cannot release given an invalid tag.
     * @throws IOException In case of error.
     */
    @Test
    public void rejectsInvalidRelease() throws IOException {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(repo.github());
        repo.releases().create("1.0");
        repo.releases().create("2.0");
        repo.releases().create("3.0-b");
        final Talk talk = CommentsTagTest.talk(issue, "4.0-alpha");
        agent.execute(talk);
        final Comment.Smart response = new Comment.Smart(
            repo.issues().get(1).comments().get(1)
        );
        MatcherAssert.assertThat(
            response.body(),
            Matchers.containsString("version tag is invalid")
        );
    }

    /**
     * CommentsTag can create a proper release message.
     * @throws Exception In case of error.
     */
    @Test
    public void createsReleaseMessage() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Agent agent = new CommentsTag(repo.github());
        final String tag = "1.5";
        final Talk talk = CommentsTagTest.talk(issue, tag);
        agent.execute(talk);
        MatcherAssert.assertThat(
            new Release.Smart(
                new Releases.Smart(repo.releases()).find(tag)
            ).body(),
            Matchers.allOf(
                Matchers.containsString("Released by Rultor"),
                Matchers.containsString(
                    "see [build log](http://www.rultor.com/t/1-abcdef)"
                )
            )
        );
    }

    /**
     * Make a talk with this tag.
     * @param issue The issue
     * @param tag The tag
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(final Issue issue, final String tag)
        throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .attr("later", "true")
                .add("wire")
                .add("href").set("http://test2").up()
                .add("github-repo").set(issue.repo().coordinates().toString())
                .up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .up()
                .add("request").attr("id", "abcdef")
                .add("type").set("release").up()
                .add("success").set("true").up()
                .add("args").add("arg").attr("name", "tag").set(tag)
        );
        return talk;
    }
}
