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
import com.jcabi.manifests.Manifests;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.Shell;
import com.rultor.agents.shells.TalkShells;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Starts daemon.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class StartsDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public StartsDaemon() {
        super(
            "/talk/shell",
            "/talk/daemon[script and not(started) and not(ended)]"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML daemon = xml.nodes("/talk/daemon").get(0);
        final Shell shell = new TalkShells(xml).get();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Shell.Safe(shell).exec(
            "mktemp -d -t rultor-XXXX",
            new NullInputStream(0L),
            baos, baos
        );
        final String dir = baos.toString(CharEncoding.UTF_8).trim();
        new Shell.Safe(shell).exec(
            String.format("cat > %s/run.sh", dir),
            IOUtils.toInputStream(
                StringUtils.join(
                    Arrays.asList(
                        "#!/bin/bash",
                        "set -x",
                        "set -e",
                        "cd $(dirname $0)",
                        "echo $$ > ./pid",
                        String.format(
                            "echo 'rultor.com %s/%s/%s'",
                            Manifests.read("Rultor-Version"),
                            Manifests.read("Rultor-Revision"),
                            Manifests.read("Rultor-Date")
                        ),
                        "date",
                        daemon.xpath("script/text()").get(0),
                        "echo 'RULTOR-SUCCESS'"
                    ),
                    "\n"
                ),
                CharEncoding.UTF_8
            ),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
        new Shell.Empty(new Shell.Safe(shell)).exec(
            StringUtils.join(
                Arrays.asList(
                    String.format("dir=%s", dir),
                    "chmod a+x ${dir}/run.sh",
                    "echo 'run.sh failed to start' > ${dir}/stdout",
                    "( nohup ${dir}/run.sh </dev/null >${dir}/stdout 2>&1 & )"
                ),
                " && "
            )
        );
        Logger.info(this, "daemon started at %s", dir);
        return new Directives().xpath("/talk/daemon").strict(1)
            .add("started")
            .set(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date()))
            .up()
            .add("dir").set(dir);
    }
}
