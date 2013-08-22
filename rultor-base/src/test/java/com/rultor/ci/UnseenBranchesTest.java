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

import com.rultor.scm.SCM;
import com.rultor.stateful.Notepad;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link UnseenBranches}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class UnseenBranchesTest {

    /**
     * UnseenBranches can show only unseen tags.
     * @throws Exception If some problem inside
     */
    @Test
    public void showsUnseenTagsOnly() throws Exception {
        final SCM origin = Mockito.mock(SCM.class);
        final String name = "master";
        Mockito.doReturn(Arrays.asList(name)).when(origin).branches();
        final Notepad notepad = Mockito.mock(Notepad.class);
        Mockito.doReturn(false).when(notepad).contains(name);
        final SCM scm = new UnseenBranches(origin, notepad);
        MatcherAssert.assertThat(scm.branches(), Matchers.hasItem(name));
        Mockito.verify(origin).branches();
        Mockito.verify(notepad).contains(name);
        Mockito.verify(notepad).add(name);
    }

    /**
     * UnseenBranches can hide seen tags.
     * @throws Exception If some problem inside
     */
    @Test
    public void hidesSeenTags() throws Exception {
        final SCM origin = Mockito.mock(SCM.class);
        Mockito.doReturn(Arrays.asList("a", "b")).when(origin).branches();
        final Notepad notepad = Mockito.mock(Notepad.class);
        Mockito.doReturn(true).when(notepad).contains(Mockito.anyString());
        final SCM scm = new UnseenBranches(origin, notepad);
        MatcherAssert.assertThat(scm.branches(), Matchers.emptyIterable());
        Mockito.verify(origin).branches();
        Mockito.verify(notepad, Mockito.times(2)).contains(Mockito.anyString());
        Mockito.verify(notepad, Mockito.never()).add(Mockito.anyString());
    }

}
