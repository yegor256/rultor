/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.TalkAgent;
import com.rultor.agents.shells.Shell;
import com.rultor.agents.shells.TalkShells;
import com.rultor.spi.Talk;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.xembly.Directives;

/**
 * Marks the daemon as done.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class EndsDaemon implements TalkAgent {

    @Override
    public void execute(final Talk talk) throws IOException {
        final XML xml = talk.read();
        if (xml.nodes("/talk/daemon").isEmpty()) {
            Logger.info(this, "there is no daemon to end");
        } else if (xml.nodes("/talk/daemon/ended").isEmpty()) {
            final Shell shell = new TalkShells().get(talk);
            final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final int exit = shell.exec(
                String.format("ps -p $(cat %s/pid)", dir),
                new NullInputStream(0L),
                baos, baos
            );
            if (exit == 0) {
                Logger.info(this, "the daemon is running in %s", dir);
            } else {
                this.end(talk, shell, dir);
            }
        } else {
            Logger.info(this, "the daemon is ended already");
        }
    }

    /**
     * End this daemon.
     * @param talk The talk
     * @param shell Shell
     * @param dir The dir
     * @throws IOException If fails
     */
    private void end(final Talk talk, final Shell shell, final String dir)
        throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int exit = shell.exec(
            String.format("grep RULTOR-SUCCESS %s/stdout", dir),
            new NullInputStream(0L),
            new ByteArrayOutputStream(), new ByteArrayOutputStream()
        );
        new Shell.Safe(shell, "failed to delete dir").exec(
            String.format("rm -rf %s", dir),
            new NullInputStream(0L),
            baos, baos
        );
        final XML xml = talk.read();
        talk.modify(
            new Directives().xpath("/talk/daemon")
                .add("ended")
                .set(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date()))
                .up()
                .add("code").set(Integer.toString(exit))
                .xpath("/talk").addIf("archive")
                .add("log")
                .attr("hash", xml.xpath("/talk/daemon/@hash").get(0))
                .set("s3://test"),
            String.format("daemon finished at %s", dir)
        );
    }

}
