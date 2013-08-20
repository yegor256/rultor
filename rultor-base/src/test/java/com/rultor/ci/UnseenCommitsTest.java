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
package com.rultor.ci;

import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.stateful.Notepad;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link UnseenCommits}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class UnseenCommitsTest {

    /**
     * UnseenCommits can show only unseen commits.
     * @throws Exception If some problem inside
     */
    @Test
    public void showsUnseenCommitsOnly() throws Exception {
        final Branch origin = Mockito.mock(Branch.class);
        Mockito.doReturn("master").when(origin).name();
        final Commit commit = Mockito.mock(Commit.class);
        final String name = "ef5eb56ef";
        Mockito.doReturn(name).when(commit).name();
        Mockito.doReturn(Arrays.asList(commit)).when(origin).log();
        final Notepad notepad = Mockito.mock(Notepad.class);
        Mockito.doReturn(false).when(notepad).contains(name);
        final Branch branch = new UnseenCommits(origin, notepad);
        MatcherAssert.assertThat(branch.log(), Matchers.hasItem(commit));
        Mockito.verify(origin).log();
        Mockito.verify(notepad).contains(name);
        Mockito.verify(notepad).add(name);
    }

    /**
     * UnseenCommits can hide seen commits.
     * @throws Exception If some problem inside
     */
    @Test
    public void hidesSeenCommits() throws Exception {
        final Branch origin = Mockito.mock(Branch.class);
        Mockito.doReturn("live").when(origin).name();
        final Commit first = Mockito.mock(Commit.class);
        Mockito.doReturn("f6ef4abe4").when(first).name();
        final Commit second = Mockito.mock(Commit.class);
        Mockito.doReturn("f6eff8c4").when(second).name();
        Mockito.doReturn(Arrays.asList(first, second)).when(origin).log();
        final Notepad notepad = Mockito.mock(Notepad.class);
        Mockito.doReturn(true).when(notepad).contains(Mockito.anyString());
        final Branch branch = new UnseenCommits(origin, notepad);
        MatcherAssert.assertThat(branch.log(), Matchers.emptyIterable());
        Mockito.verify(origin).log();
        Mockito.verify(notepad, Mockito.times(2)).contains(Mockito.anyString());
        Mockito.verify(notepad, Mockito.never()).add(Mockito.anyString());
    }

}
