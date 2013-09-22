/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.cd.jira;

import java.util.Date;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link RxJira}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RxJiraITCase {

    /**
     * JIRA URL.
     */
    private static final String URL =
        System.getProperty("failsafe.jira.url");

    /**
     * JIRA test issue.
     */
    private static final String KEY =
        System.getProperty("failsafe.jira.issue");

    /**
     * RxJira can retrieve issues.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesIssuesFromJira() throws Exception {
        Assume.assumeNotNull(RxJiraITCase.URL);
        final Jira jira = new RxJira(RxJiraITCase.URL);
        final Iterable<JiraIssue> issues = jira.search(
            String.format("key = %s", RxJiraITCase.KEY)
        );
        MatcherAssert.assertThat(
            issues,
            Matchers.<JiraIssue>iterableWithSize(1)
        );
        for (JiraIssue issue : issues) {
            MatcherAssert.assertThat(
                issue.key(),
                Matchers.equalTo(RxJiraITCase.KEY)
            );
            MatcherAssert.assertThat(
                issue.comments(),
                Matchers.not(Matchers.emptyIterable())
            );
            for (JiraComment comment : issue.comments()) {
                MatcherAssert.assertThat(
                    comment.body(),
                    Matchers.notNullValue()
                );
                MatcherAssert.assertThat(
                    comment.author(),
                    Matchers.notNullValue()
                );
            }
        }
    }

    /**
     * RxJira can post a comment.
     * @throws Exception If some problem inside
     */
    @Test
    public void postsCommentToJiraIssue() throws Exception {
        final JiraIssue issue = this.issue();
        issue.post(String.format("test message, ignore it, %s", new Date()));
    }

    /**
     * RxJira can assign issue to someone else.
     * @throws Exception If some problem inside
     */
    @Test
    public void assignsIssueToAnotherPerson() throws Exception {
        final JiraIssue issue = this.issue();
        issue.assign(issue.comments().iterator().next().author());
    }

    /**
     * Get issue.
     * @return Issue to test against
     * @throws Exception If some problem inside
     */
    private JiraIssue issue() throws Exception {
        Assume.assumeNotNull(RxJiraITCase.URL);
        final Jira jira = new RxJira(RxJiraITCase.URL);
        return jira.search(
            String.format("key= %s", RxJiraITCase.KEY)
        ).iterator().next();
    }

}
