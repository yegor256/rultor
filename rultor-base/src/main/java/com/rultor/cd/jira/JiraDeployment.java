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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.cd.Deployment;
import com.rultor.ext.jira.JiraIssue;
import com.rultor.snapshot.Radar;
import com.rultor.snapshot.TagLine;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
        new TagLine("jira").attr("key", iss.key()).log();
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
    public void started() {
        this.issue.post(
            Radar.render(
                this.getClass().getResourceAsStream("jira-started.xsl")
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void succeeded() {
        this.issue.revert(
            Radar.render(
                this.getClass().getResourceAsStream("jira-succeeded.xsl")
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failed() {
        this.issue.revert(
            Radar.render(
                this.getClass().getResourceAsStream("jira-failed.xsl")
            )
        );
    }

}
