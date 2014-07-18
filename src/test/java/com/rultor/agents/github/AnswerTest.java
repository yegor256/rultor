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
package com.rultor.agents.github;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import java.io.IOException;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for ${@link Answer}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.8.16
 */
public final class AnswerTest {

    /**
     * Answer can post a message.
     * @throws Exception In case of error.
     */
    @Test
    public void postsGithubComment() throws Exception {
        final Issue issue = AnswerTest.issue();
        issue.comments().post("hey, do it");
        new Answer(new Comment.Smart(issue.comments().get(1))).post("hey");
        MatcherAssert.assertThat(
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.containsString("> hey, do it\n\n")
        );
    }

    /**
     * Answer can reject a message if it's a spam from us.
     * @throws Exception In case of error.
     */
    @Test(expected = IllegalStateException.class)
    public void preventsSpam() throws Exception {
        final Issue issue = AnswerTest.issue();
        issue.comments().post("hey, do it");
        final Comment.Smart comment = new Comment.Smart(issue.comments().get(1));
        new Answer(comment).post("first");
        new Answer(comment).post("second");
        new Answer(comment).post("this one should be rejected");
    }

    /**
     * Make an issue.
     * @return Issue
     * @throws IOException If fails
     */
    private static Issue issue() throws IOException {
        final Repo repo = new MkGithub("jeff").repos().create(
            Json.createObjectBuilder().add("name", "test").build()
        );
        return repo.issues().create("", "");
    }

}
