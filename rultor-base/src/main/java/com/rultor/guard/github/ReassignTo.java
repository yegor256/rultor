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
package com.rultor.guard.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.snapshot.Step;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Reassigns to user.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "user")
@Loggable(Loggable.DEBUG)
public final class ReassignTo implements Approval {

    /**
     * Login to reassign to.
     */
    private final transient String user;

    /**
     * Public ctor.
     * @param login Login to assign to
     */
    public ReassignTo(
        @NotNull(message = "login can not be null") final String login) {
        this.user = login;
    }

    @Override
    @Step("assigned pull request #${args[0].number} to `${args[0].user.login}`")
    public boolean has(final PullRequest request, final Github client,
        final Github.Repo repo) throws IOException {
        final IssueService svc = new IssueService(client.client());
        final Issue issue = svc.getIssue(repo, request.getNumber());
        User assignee = issue.getAssignee();
        if (assignee == null) {
            assignee = new User();
            assignee.setLogin("");
        }
        if (!assignee.getLogin().equals(this.user)) {
            assignee.setLogin(this.user);
            issue.setAssignee(assignee);
            try {
                svc.editIssue(repo, issue);
            } catch (final RequestException ex) {
                if ("Server Error (500)".equals(ex.getMessage())) {
                    Logger.warn(this, "strange error on Github side: %s", ex);
                } else {
                    throw ex;
                }
            }
        }
        return true;
    }

}
