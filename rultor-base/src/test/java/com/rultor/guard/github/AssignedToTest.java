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

import java.io.IOException;
import javax.validation.ConstraintViolationException;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link AssignedTo}.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
public final class AssignedToTest {

    /**
     * AssignedTo args cannot be null.
     * @throws Exception If some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void argsCanNotBeNull() throws Exception {
        new AssignedTo(null);
    }

    /**
     * AssignedTo can approve for assigned user.
     * @throws IOException If some problem inside
     */
    @Test
    public void canApproveAssigned() throws IOException {
        final String test = "test";
        final Approval approval = new AssignedTo(test);
        final PullRequest request = Mockito.mock(PullRequest.class);
        final Github client = Mockito.mock(Github.class);
        final User assignee = Mockito.mock(User.class);
        Mockito.when(assignee.getLogin()).thenReturn(test);
        Mockito.when(request.getAssignee()).thenReturn(assignee);
        MatcherAssert.assertThat(
            approval.has(
                request ,
                client ,
                new Github.Repo("xembly/xembly")
            ),
            Matchers.is(true)
        );
    }

    /**
     * AssignedTo rejects unassigned user.
     * @throws IOException If some problem inside
     */
    @Test
    public void rejectUnAssigned() throws IOException {
        final String test = "test1";
        final Approval approval = new AssignedTo(test);
        final PullRequest request = Mockito.mock(PullRequest.class);
        final Github client = Mockito.mock(Github.class);
        final User assignee = Mockito.mock(User.class);
        Mockito.when(assignee.getLogin()).thenReturn("test2");
        Mockito.when(request.getAssignee()).thenReturn(assignee);
        MatcherAssert.assertThat(
            approval.has(
                request ,
                client ,
                new Github.Repo("xembly1/xembly")
            ),
            Matchers.is(false)
        );
    }
}
