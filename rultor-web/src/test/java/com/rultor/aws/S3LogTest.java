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
package com.rultor.aws;

import com.jcabi.urn.URN;
import com.rultor.spi.Conveyer;
import com.rultor.spi.Spec;
import com.rultor.spi.Work;
import java.util.logging.Level;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link S3Log}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class S3LogTest {

    /**
     * S3Log can log (the test is not finished).
     * @throws Exception If some problem inside
     */
    @Test
    @org.junit.Ignore
    public void logsMessages() throws Exception {
        final Spec spec = new Spec.Simple("java.lang.Integer(5)");
        final String unit = "some-test-unit";
        final URN owner = new URN("urn:facebook:1");
        final Work work = new Work.Simple(owner, unit, spec);
        final String msg = "some test log message \u20ac";
        final S3Client client = Mockito.mock(S3Client.class);
        final S3Log log = new S3Log(client);
        log.push(
            work,
            new Conveyer.Line.Simple(0, Level.INFO, msg)
        );
    }

}
