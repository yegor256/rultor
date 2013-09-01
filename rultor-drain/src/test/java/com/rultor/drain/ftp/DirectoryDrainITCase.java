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
package com.rultor.drain.ftp;

import com.jcabi.urn.URN;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link DirectoryDrain}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class DirectoryDrainITCase {

    /**
     * FTP host.
     */
    private final transient String host =
        System.getProperty("failsafe.ftp.host");

    /**
     * FTP user name.
     */
    private final transient String login =
        System.getProperty("failsafe.ftp.login");

    /**
     * FTP password.
     */
    private final transient String password =
        System.getProperty("failsafe.ftp.password");

    /**
     * FTP directory.
     */
    private final transient String dir =
        FilenameUtils.getFullPathNoEndSeparator(
            System.getProperty("failsafe.ftp.file")
        );

    /**
     * DirectoryDrain can log.
     * @throws Exception If some problem inside
     */
    @Test
    public void logsMessages() throws Exception {
        Assume.assumeNotNull(this.host);
        final String msg = "some test log message \u20ac";
        final Work work = new Work.Simple(
            new URN("urn:test:1"), "some-rule", new Time(1)
        );
        final Drain drain = new DirectoryDrain(
            work, this.host, this.login, this.password, this.dir
        );
        drain.append(Arrays.asList(msg));
        final Pageable<Time, Time> names = drain.pulses();
        MatcherAssert.assertThat(names, Matchers.hasItem(work.scheduled()));
        MatcherAssert.assertThat(
            IOUtils.toString(drain.read(), CharEncoding.UTF_8),
            Matchers.containsString(msg)
        );
    }

}
