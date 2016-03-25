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
package com.rultor.agents.hn;

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for {@link HnUpdates}.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.58
 */
public final class HnUpdatesTest {

    /**
     * HnUpdate can post.
     * @throws Exception In case of error.
     */
    @Test
    public void postsUpdate() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final HackerNews news = Mockito.mock(HackerNews.class);
        final Talk talk = HnUpdatesTest.talk(
            repo, repo.issues().create("", "")
        );
        new HnUpdates(repo.github(), news).execute(talk);
        Mockito.verify(news).post(
            Matchers.contains(repo.coordinates().repo()),
            Matchers.contains(repo.coordinates().repo())
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
                .add("msec").set("1234567").up()
                .add("success").set("true").up()
                .add("type").set("release").up()
                .add("args").add("arg").attr("name", "tag").set("1.7")
        );
        return talk;
    }
}
