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
import com.jcabi.xml.XML;
import com.rultor.agents.TalkAgent;
import com.rultor.agents.shells.Shell;
import com.rultor.agents.shells.TalkShells;
import com.rultor.spi.Talk;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.io.input.NullInputStream;
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
        if (!xml.nodes("/talk/daemon[not(@done)]").isEmpty()) {
            final Shell shell = new TalkShells().get(talk);
            final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            shell.exec(
                String.format("ps -p $(cat %s/pid)", dir),
                new NullInputStream(0L),
                baos, baos
            );
            if (baos.size() == 0) {
                baos.reset();
                final boolean success = 0 == shell.exec(
                    String.format("grep RULTOR-SUCCESS %s/stdout", dir),
                    new NullInputStream(0L),
                    new ByteArrayOutputStream(), new ByteArrayOutputStream()
                );
                shell.exec(
                    String.format("rm -rf %s", dir),
                    new NullInputStream(0L),
                    baos, baos
                );
                talk.modify(
                    new Directives().xpath("/talk/daemon")
                        .attr("done", "yes")
                        .add("success").set(Boolean.toString(success)),
                    String.format("daemon finished at %s", dir)
                );
            }
        }
    }
}
