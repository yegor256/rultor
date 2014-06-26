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
package com.rultor.guard.jira;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.ext.jira.JiraComment;
import com.rultor.ext.jira.JiraIssue;
import com.rultor.guard.MergeRequest;
import com.rultor.scm.Branch;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Uni-direct merging.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "msg", "uri" })
@Loggable(Loggable.DEBUG)
public final class TargetPosted implements Refinement {

    /**
     * Destination branch pattern.
     */
    private final transient String msg;

    /**
     * URI of destination.
     */
    private final transient String uri;

    /**
     * Public ctor.
     * @param ptn Pattern for the message to search
     * @param scm URI of SCM
     */
    public TargetPosted(final String ptn, final String scm) {
        this.msg = ptn;
        this.uri = scm;
    }

    @Override
    public MergeRequest refine(final MergeRequest request,
        final JiraIssue issue) {
        // @checkstyle AnonInnerLength (50 lines)
        return new MergeRequest() {
            @Override
            public String name() {
                return request.name();
            }
            @Override
            public Branch source() {
                return request.source();
            }
            @Override
            public Branch destination() {
                return new Branch.Passive(
                    URI.create(TargetPosted.this.uri),
                    TargetPosted.this.branch(issue)
                );
            }
            @Override
            public void started() throws IOException {
                TargetPosted.this.branch(issue);
                request.started();
            }
            @Override
            public void accept() throws IOException {
                request.accept();
            }
            @Override
            public void reject() throws IOException {
                request.reject();
            }
        };
    }
    /**
     * Get destination branch from the issue.
     * @param issue The issue
     * @return Destination branch name
     */
    private String branch(final JiraIssue issue) {
        final Iterable<JiraComment> comments = issue.comments();
        if (Iterables.isEmpty(comments)) {
            issue.revert(
                // @checkstyle LineLength (1 line)
                "Please, tell me which branch you want it to be merge into"
            );
            throw new IllegalStateException(
                String.format(
                    "destination branch is not specified in %s", issue
                )
            );
        }
        final String body = comments.iterator().next().body();
        final Matcher matcher = Pattern.compile(this.msg).matcher(body);
        if (!matcher.matches()) {
            issue.revert(
                String.format(
                    "I didn't get what you said here: \"%s\"", body
                )
            );
            throw new IllegalStateException(
                String.format(
                    "message is not parseable: '%s'", body
                )
            );
        }
        return matcher.group(1);
    }
}
