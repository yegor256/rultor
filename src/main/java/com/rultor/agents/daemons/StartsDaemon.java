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
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directives;

/**
 * Starts daemon.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class StartsDaemon implements TalkAgent {

    @Override
    public void execute(final Talk talk) throws IOException {
        final XML xml = talk.read();
        if (!xml.nodes("/talk/daemon[not(started)]").isEmpty()) {
            final XML daemon = xml.nodes("/talk/daemon").get(0);
            final Shell shell = new TalkShells().get(talk);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            shell.exec(
                StringUtils.join(
                    Arrays.asList(
                        "dir=$(mktemp -d -t rultor)",
                        "cat > ${dir}/run.sh",
                        "chmod a+x ${dir}/run.sh",
                        "echo ${dir}",
                        "nohup ${dir}/run.sh > ${dir}/stdout 2> ${dir}/stderr &"
                    ),
                    "; "
                ),
                IOUtils.toInputStream(
                    StringUtils.join(
                        Arrays.asList(
                            "#!/bin/bash",
                            "set -x",
                            "set -e",
                            "echo $$ > ./pid",
                            daemon.xpath("script/text()").get(0),
                            "echo 'RULTOR-SUCCESS'"
                        ),
                        "\n"
                    ),
                    CharEncoding.UTF_8
                ),
                baos, baos
            );
            talk.modify(
                new Directives().xpath("/talk/daemon").strict(1)
                    .add("started").set(new Date().toString()).up()
                    .add("dir").set(baos.toString(CharEncoding.UTF_8))
            );
        }
    }
}
