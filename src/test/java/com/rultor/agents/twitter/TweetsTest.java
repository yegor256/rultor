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
package com.rultor.agents.twitter;

import com.google.common.collect.Lists;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Issues;
import com.jcabi.github.Language;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for {@link Tweets}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.30
 * @todo #561 When all the puzzles from
 *  https://github.com/jcabi/jcabi-github/issues/923 are implemented, remove
 *  repo, issue, lang methods, and use MkGithub family in all the tests. Also
 *  enable postsTweet method.
 */
public final class TweetsTest {

    /**
     * Tweets can post a tweet.
     * @throws Exception In case of error.
     */
    @Test
    @Ignore
    public void postsTweet() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final Twitter twitter = Mockito.mock(Twitter.class);
        final Agent agent = new Tweets(repo.github(), twitter);
        final Talk talk = this.talk(repo, issue);
        agent.execute(talk);
        Mockito.verify(twitter).post(
            Matchers.contains("test")
        );
    }

    /**
     * Tweets can post a tweet with language tags.
     * @throws Exception In case of error.
     */
    @Test
    public void postsTweetWithLanguages() throws Exception {
        final Repo repo = this.repo();
        final List<Language> langs = Lists.newArrayList(
            this.lang("Java"), this.lang("Python")
        );
        Mockito.when(repo.languages()).thenReturn(langs);
        final Twitter twitter = Mockito.mock(Twitter.class);
        final Agent agent = new Tweets(repo.github(), twitter);
        final Talk talk = this.talk(repo, this.issue(repo));
        agent.execute(talk);
        Mockito.verify(twitter).post(
            Matchers.contains(
                String.format("#%s #%s", langs.get(0), langs.get(1))
            )
        );
    }

    /**
     * Create mock issue.
     * @param repo Repo to use
     * @return Mocked issue.
     */
    private Issue issue(final Repo repo) {
        final Issues issues = Mockito.mock(Issues.class);
        final Issue issue = Mockito.mock(Issue.class);
        Mockito.when(issue.repo()).thenReturn(repo);
        Mockito.when(issues.get(Mockito.anyInt())).thenReturn(issue);
        Mockito.when(repo.issues()).thenReturn(issues);
        return issue;
    }

    /**
     * Create mock repo.
     * @return Mocked repo
     * @throws IOException In case of error
     */
    private Repo repo() throws IOException {
        final Github github = Mockito.mock(Github.class);
        final Repo repo = Mockito.mock(Repo.class);
        final JsonObject rjson = Json.createObjectBuilder()
            .add("description", "something").build();
        Mockito.when(repo.json()).thenReturn(rjson);
        Mockito.when(repo.github()).thenReturn(github);
        final Repos repos = Mockito.mock(Repos.class);
        Mockito.when(github.repos()).thenReturn(repos);
        final Coordinates coords = new Coordinates.Simple("foo/bar");
        Mockito.when(repo.coordinates()).thenReturn(coords);
        Mockito.when(repos.get(Mockito.any(Coordinates.class)))
            .thenReturn(repo);
        return repo;
    }

    /**
     * Creates a talk with repo and issue.
     * @param repo Repo to use
     * @param issue Issue to use
     * @return Created Talk
     * @throws IOException In case of error
     */
    private Talk talk(final Repo repo, final Issue issue) throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("wire")
                .add("href").set("http://test").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .up()
                .add("request").attr("id", "1")
                .add("msec").set("1234567").up()
                .add("success").set("true").up()
                .add("type").set("release").up()
                .add("args").add("arg").attr("name", "tag").set("1.7")
        );
        return talk;
    }

    /**
     * Create mock language.
     * @param name Name of the language
     * @return Language created.
     */
    private Language lang(final String name) {
        return new Language() {
            @Override
            public String name() {
                return name;
            }
            @Override
            public long bytes() {
                return 0;
            }
        };
    }
}
