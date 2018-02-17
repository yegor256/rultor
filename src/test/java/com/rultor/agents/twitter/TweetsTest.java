/**
 * Copyright (c) 2009-2018, rultor.com
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

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for {@link Tweets}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.30
 */
public final class TweetsTest {

    /**
     * Tweets can post a tweet.
     * @throws Exception In case of error.
     */
    @Test
    public void postsTweet() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Twitter twitter = Mockito.mock(Twitter.class);
        final Talk talk = TweetsTest.talk(repo, repo.issues().create("", ""));
        new Tweets(repo.github(), twitter).execute(talk);
        Mockito.verify(twitter).post(
            Matchers.contains(repo.coordinates().repo())
        );
    }

    /**
     * Tweets can post a tweet with language tags.
     * @throws Exception In case of error.
     */
    @Test
    public void postsTweetWithLanguages() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Twitter twitter = Mockito.mock(Twitter.class);
        new Tweets(repo.github(), twitter).execute(
            TweetsTest.talk(repo, repo.issues().create("", ""))
        );
        Mockito.verify(twitter).post(
            Matchers.contains(
                new JoinedText(
                    " ",
                    new Mapped<>(
                        lang -> String.format("#%s", lang.name()),
                        repo.languages()
                    )
                ).asString()
            )
        );
    }

    /**
     * Creates a talk with repo and issue.
     * @param repo Repo to use
     * @param issue Issue to use
     * @return Created Talk
     * @throws IOException In case of error
     */
    private static Talk talk(final Repo repo, final Issue issue)
        throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("wire")
                .add("href").set("http://test").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .up()
                .add("request").attr("id", "1")
                .add("author").set("yegor256").up()
                .add("msec").set("1234567").up()
                .add("success").set("true").up()
                .add("type").set("release").up()
                .add("args").add("arg").attr("name", "tag").set("1.7")
        );
        return talk;
    }
}
