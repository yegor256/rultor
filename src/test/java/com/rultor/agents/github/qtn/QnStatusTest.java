/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.agents.github.Req;
import com.rultor.spi.Talk;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnStatus}.
 *
 * @since 1.5
 */
final class QnStatusTest {

    /**
     * QnStatus can build a report.
     * @throws Exception In case of error.
     */
    @Test
    void buildsReport() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("status");
        final Talk talk = new Talk.InFile(
            "<talk name='test' number='45' later='false'>",
            "<request id='454'><type>merge</type><args/>",
            "<author>yegor256</author></request>",
            "<daemon id='454'><started>2014-07-08T12:09:09Z</started>",
            "<script>test</script><title>something</title>",
            "<code>3</code><dir>/tmp/abc</dir>",
            "</daemon>",
            "</talk>"
        );
        MatcherAssert.assertThat(
            "Request should have done status",
            new QnWithAuthor(new QnStatus(talk)).understand(
                new Comment.Smart(issue.comments().get(1)),
                new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Current status should be posted in the comment",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.allOf(
                Matchers.containsString("request `454` is in processing"),
                Matchers.containsString("request has no parameters"),
                Matchers.containsString("build started"),
                Matchers.containsString("build exit code is `3`")
            )
        );
    }

}
