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
import com.rultor.snapshot.Radar;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.XSLT;
import javax.xml.transform.TransformerException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.ImpossibleModificationException;
import org.xembly.XemblySyntaxException;

/**
 * Three XSL posts into JIRA issue.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "start", "success", "failure" })
@Loggable(Loggable.DEBUG)
public final class XslPosts implements Refinement {

    /**
     * XSL for started.
     */
    private final transient String start;

    /**
     * XSL for success.
     */
    private final transient String success;

    /**
     * XSL for failure.
     */
    private final transient String failure;

    /**
     * Public ctor.
     * @param started XSL for "started" post
     * @param accept XSL for "accept" post
     * @param reject XSL for "reject" post
     */
    public XslPosts(final String started, final String accept,
        final String reject) {
        this.start = started;
        this.success = accept;
        this.failure = reject;
    }

    @Override
    public MergeRequest refine(final MergeRequest request,
        final JiraIssue issue) {
        // @checkstyle AnonInnerLength (50 lines)
        return new MergeRequest() {
            private final transient String key = issue.key();
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
                return request.destination();
            }
            @Override
            @Step("notified JIRA issue ${this.key} that merging started")
            public void started() {
                issue.post(XslPosts.render(XslPosts.this.start));
            }
            @Override
            @Step("accepted JIRA request in ${this.key}")
            public void accept() {
                issue.revert(XslPosts.render(XslPosts.this.success));
            }
            @Override
            @Step("rejected JIRA request in ${this.key}")
            public void reject() {
                issue.revert(XslPosts.render(XslPosts.this.failure));
            }
        };
    }
    /**
     * Render current snapshot using provided XSL.
     * @param xsl XSL to use
     * @return Snapshot rendered
     */
    private static String render(final String xsl) {
        try {
            return new XSLT(Radar.snapshot(), xsl).xml();
        } catch (TransformerException ex) {
            throw new IllegalStateException(ex);
        } catch (ImpossibleModificationException ex) {
            throw new IllegalStateException(ex);
        } catch (XemblySyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
