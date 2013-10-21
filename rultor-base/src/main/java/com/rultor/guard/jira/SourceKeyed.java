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
package com.rultor.guard.jira;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.ext.jira.JiraIssue;
import com.rultor.guard.MergeRequest;
import com.rultor.scm.Branch;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Source branch detected from issue key.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "uri")
@Loggable(Loggable.DEBUG)
public final class SourceKeyed implements Refinement {

    /**
     * URI of source.
     */
    private final transient String uri;

    /**
     * Public ctor.
     * @param scm URI of SCM
     */
    public SourceKeyed(final String scm) {
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
                return new Branch.Passive(
                    URI.create(SourceKeyed.this.uri),
                    issue.key()
                );
            }
            @Override
            public Branch destination() {
                return request.destination();
            }
            @Override
            public void started() throws IOException {
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
}
