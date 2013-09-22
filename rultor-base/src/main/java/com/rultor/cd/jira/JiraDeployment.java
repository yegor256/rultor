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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.cd.Deployment;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XSLT;
import com.rultor.tools.Exceptions;
import java.util.Map;
import javax.xml.transform.TransformerException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.ImpossibleModificationException;

/**
 * Deployment request from JIRA.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "issue")
@Loggable(Loggable.DEBUG)
final class JiraDeployment implements Deployment {

    /**
     * JIRA issue.
     */
    private final transient JiraIssue issue;

    /**
     * Public ctor.
     * @param iss JIRA issue
     */
    protected JiraDeployment(final JiraIssue iss) {
        this.issue = iss;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.issue.key();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> params() {
        return new ImmutableMap.Builder<String, Object>()
            .put("name", this.name())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void succeeded(final Snapshot snapshot) {
        this.issue.assign(this.issue.comments().iterator().next().author());
        this.issue.post(this.summary(snapshot));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failed(final Snapshot snapshot) {
        this.issue.assign(this.issue.comments().iterator().next().author());
        this.issue.post(this.summary(snapshot));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminated() {
        this.issue.post("Deployment terminated");
    }

    /**
     * Make summary out of snapshot.
     * @param snapshot Snapshot XML
     * @return Summary
     */
    private String summary(final Snapshot snapshot) {
        String summary;
        try {
            summary = new XSLT(
                snapshot,
                this.getClass().getResourceAsStream("deploy-summary.xsl")
            ).xml();
        } catch (TransformerException ex) {
            summary = Exceptions.stacktrace(ex);
        } catch (ImpossibleModificationException ex) {
            summary = Exceptions.stacktrace(ex);
        }
        return summary;
    }

}
