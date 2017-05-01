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
package com.rultor.base;

import com.jcabi.urn.URN;
import com.rultor.spi.Instance;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.net.URI;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link Descriptive}.
 * @author Gangababu Tirumalanadhuni (gangababu.t@gmail.com)
 * @version $Id$
 */
public final class DescriptiveTest {

    /**
     * Test descriptives in Xembly Log.
     * @throws Exception If some problem inside
     */
    @Test
    public void testDescriptiveInXemblyLog() throws Exception {
        final Time scheduled = new Time();
        final Instance origin = Mockito.mock(Instance.class);
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock inv)
                    throws Exception {
                    return null;
                }
            }
        ).when(origin).pulse();
        final Work work = Mockito.mock(Work.class);
        Mockito.doAnswer(
            new Answer<URI>() {
                @Override
                public URI answer(final InvocationOnMock inv)
                    throws Exception {
                    return URI.create("urn:facebook:1");
                }
            }
        ).when(work).stdout();
        Mockito.doAnswer(
            new Answer<Time>() {
                @Override
                public Time answer(final InvocationOnMock inv)
                    throws Exception {
                    return scheduled;
                }
            }
        ).when(work).scheduled();
        Mockito.doAnswer(
            new Answer<URN>() {
                @Override
                public URN answer(final InvocationOnMock inv)
                    throws Exception {
                    return URN.create("urn:facebook:2");
                }
            }
        ).when(work).owner();
        Mockito.doAnswer(
            new Answer<String>() {
                @Override
                public String answer(final InvocationOnMock inv)
                    throws Exception {
                    return "test-unit";
                }
            }
        ).when(work).unit();
        final Descriptive descriptive = new Descriptive(
            work,
            origin
        );
        descriptive.pulse();
        Mockito.verify(origin, Mockito.times(1)).pulse();
        Mockito.verify(work, Mockito.times(1)).unit();
        Mockito.verify(work, Mockito.times(1)).scheduled();
        Mockito.verify(work, Mockito.times(1)).stdout();
        Mockito.verify(work, Mockito.times(1)).owner();
    }

}
