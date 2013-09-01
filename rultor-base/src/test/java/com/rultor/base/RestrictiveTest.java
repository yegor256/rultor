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
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Restrictive}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RestrictiveTest {

    /**
     * Restrictive can pass through when it's allowed.
     * @throws Exception If some problem inside
     */
    @Test
    public void passesThroughWhenAllowed() throws Exception {
        final Object origin = Mockito.mock(Object.class);
        final Work work = new Work.Simple(new URN("urn:test:3"), "test-rule");
        new Restrictive(work, Arrays.asList("urn:test:*"), origin).toString();
    }

    /**
     * Restrictive can pass through when exact URN provided.
     * @throws Exception If some problem inside
     */
    @Test
    public void passesThroughWhenAllowedForExactUrn() throws Exception {
        final Object origin = Mockito.mock(Object.class);
        final String owner = "urn:test:777";
        final Work work = new Work.Simple(new URN(owner), "test-A");
        new Restrictive(work, Arrays.asList(owner), origin).toString();
    }

    /**
     * Restrictive can pass through when it's allowed.
     * @throws Exception If some problem inside
     */
    @Test
    public void passesThroughWhenAllowedForAll() throws Exception {
        final Instance origin = Mockito.mock(Instance.class);
        final Work work = new Work.Simple(new URN("urn:test:33"), "test-43");
        new Restrictive(work, Arrays.asList("*"), origin).object();
    }

    /**
     * Restrictive can block when it's necessary.
     * @throws Exception If some problem inside
     */
    @Test(expected = SecurityException.class)
    public void blocksWhenNotAllowed() throws Exception {
        final Instance origin = Mockito.mock(Instance.class);
        final Work work = new Work.Simple(new URN("urn:test:6"), "test-44");
        new Restrictive(work, Arrays.asList("urn:facebook:2"), origin).object();
    }

    /**
     * Restrictive can reject when entirely disabled.
     * @throws Exception If some problem inside
     */
    @Test(expected = SecurityException.class)
    public void blocksEverybodyWhenRequested() throws Exception {
        final Instance origin = Mockito.mock(Instance.class);
        final Work work = new Work.Simple(new URN("urn:test:998"), "test-8");
        new Restrictive(work, new ArrayList<String>(0), origin).toString();
    }

}
