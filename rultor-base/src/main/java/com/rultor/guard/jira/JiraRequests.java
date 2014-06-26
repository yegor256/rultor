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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.ext.jira.Jira;
import com.rultor.ext.jira.JiraIssue;
import com.rultor.guard.MergeRequest;
import com.rultor.guard.MergeRequests;
import com.rultor.scm.Branch;
import java.util.Collection;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Merge requests in JIRA.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = {"jira", "jql", "refinement" })
@Loggable(Loggable.DEBUG)
public final class JiraRequests implements MergeRequests {

    /**
     * JIRA client.
     */
    private final transient Jira jira;

    /**
     * JQL query.
     */
    private final transient String jql;

    /**
     * Refinement to use.
     */
    private final transient Refinement refinement;

    /**
     * Public ctor.
     * @param jra JIRA client
     * @param query JQL query
     * @param ref Refinement
     */
    public JiraRequests(final Jira jra, final String query,
        final Refinement ref) {
        this.jira = jra;
        this.jql = query;
        this.refinement = ref;
    }

    /**
     * Public ctor.
     * @param jra JIRA client
     * @param query JQL query
     * @param refs Refinements
     */
    public JiraRequests(final Jira jra, final String query,
        final Collection<Refinement> refs) {
        this(
            jra, query,
            new Refinement() {
                @Override
                public MergeRequest refine(final MergeRequest request,
                    final JiraIssue issue) {
                    MergeRequest refined = request;
                    for (final Refinement ref : refs) {
                        refined = ref.refine(refined, issue);
                    }
                    return refined;
                }
            }
        );
    }

    @Override
    public Iterator<MergeRequest> iterator() {
        return Iterators.transform(
            this.jira.search(this.jql).iterator(),
            new Function<JiraIssue, MergeRequest>() {
                @Override
                public MergeRequest apply(final JiraIssue issue) {
                    return JiraRequests.this.refinement.refine(
                        new JiraRequests.Empty(issue), issue
                    );
                }
            }
        );
    }

    /**
     * Empty merge request.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "issue")
    @Loggable(Loggable.DEBUG)
    private static final class Empty implements MergeRequest {
        /**
         * Jira issue.
         */
        private final transient JiraIssue issue;
        /**
         * Public ctor.
         * @param iss Issue
         */
        protected Empty(final JiraIssue iss) {
            this.issue = iss;
        }
        @Override
        public String name() {
            return this.issue.key();
        }
        @Override
        public Branch source() {
            throw new UnsupportedOperationException("source()");
        }
        @Override
        public Branch destination() {
            throw new UnsupportedOperationException("destination()");
        }
        @Override
        public void started() {
            assert this.issue != null;
        }
        @Override
        public void accept() {
            assert this.issue != null;
        }
        @Override
        public void reject() {
            assert this.issue != null;
        }
    }

}
