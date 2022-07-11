/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor;

import co.stateful.RtSttc;
import com.jcabi.github.RtGithub;
import com.jcabi.urn.URN;
import org.junit.Test;

/**
 * Test case for {@link Entry}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.58
 */
public final class EntryTest {

    /**
     * RtSttc can work in production mode.
     *
     * <p>This test is actually checking not how RtSttc works, but
     * whether it can work in current environment, with full list
     * of project dependencies. If there will be any dependency issue,
     * this test will crash with a different exception, not AssertionError.
     *
     */
    @Test(expected = AssertionError.class)
    public void sttcConnects() throws Exception {
        new RtSttc(
            URN.create("urn:test:1"),
            "invalid-token"
        ).counters().names();
    }

    /**
     * RtGithub can work in production mode.
     *
     * <p>This test is actually checking not how RtGithug works, but
     * whether it can work in current environment, with full list
     * of project dependencies. If there will be any dependency issue,
     * this test will crash with a different exception, not AssertionError.
     *
     */
    @Test(expected = AssertionError.class)
    public void githubConnects() throws Exception {
        new RtGithub("intentionally-invalid-token").users().self().login();
    }

}
