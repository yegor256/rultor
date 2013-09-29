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
package com.rultor.guard;

import com.rultor.scm.Branch;
import com.rultor.scm.SCM;
import com.rultor.shell.Batch;
import com.rultor.spi.Instance;
import com.rultor.stateful.ConcurrentNotepad;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link OnPullRequest}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class OnPullRequestTest {

    /**
     * OnPullRequest can build when request is available.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void failsOnNewPullRequest() throws Exception {
        final MergeRequests requests = Mockito.mock(MergeRequests.class);
        final MergeRequest request = this.request();
        Mockito.doReturn(Arrays.asList(request).iterator())
            .when(requests).iterator();
        final Batch batch = Mockito.mock(Batch.class);
        Mockito.doReturn(1).when(batch)
            .exec(Mockito.any(Map.class), Mockito.any(OutputStream.class));
        final ConcurrentNotepad notepad = Mockito.mock(ConcurrentNotepad.class);
        Mockito.doReturn(true).when(notepad).addIf(Mockito.anyString());
        final Instance instance = new OnPullRequest(requests, notepad, batch);
        instance.pulse();
        Mockito.verify(batch).exec(
            Mockito.any(Map.class), Mockito.any(OutputStream.class)
        );
        Mockito.verify(request).reject();
    }

    /**
     * OnPullRequest can build when request is available.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void succeedsOnNewPullRequest() throws Exception {
        final MergeRequests requests = Mockito.mock(MergeRequests.class);
        final MergeRequest request = this.request();
        Mockito.doReturn(Arrays.asList(request).iterator())
            .when(requests).iterator();
        final Batch batch = Mockito.mock(Batch.class);
        final ConcurrentNotepad notepad = Mockito.mock(ConcurrentNotepad.class);
        Mockito.doReturn(true).when(notepad).addIf(Mockito.anyString());
        final Instance instance = new OnPullRequest(requests, notepad, batch);
        instance.pulse();
        Mockito.verify(batch).exec(
            Mockito.any(Map.class), Mockito.any(OutputStream.class)
        );
        Mockito.verify(request).accept();
    }

    /**
     * Make a fake request.
     * @return Merge request
     * @throws Exception If some problem inside
     */
    private MergeRequest request() throws Exception {
        final MergeRequest request = Mockito.mock(MergeRequest.class);
        Mockito.doReturn("#626").when(request).name();
        final SCM scm = Mockito.mock(SCM.class);
        Mockito.doReturn(new URI("ssh://git@github.com/rultor/rultor.git"))
            .when(scm).uri();
        final Branch branch = Mockito.mock(Branch.class);
        Mockito.doReturn("master").when(branch).name();
        Mockito.doReturn(scm).when(branch).scm();
        Mockito.doReturn(branch).when(request).source();
        Mockito.doReturn(branch).when(request).destination();
        return request;
    }

}
