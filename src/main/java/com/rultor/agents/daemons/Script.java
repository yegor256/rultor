/**
 * Copyright (c) 2009-2018, rultor.com
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
import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.agents.shells.TalkShells;
import java.io.IOException;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.text.JoinedText;

/**
 * Script to run.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.53
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
final class Script {

    /**
     * Script name.
     */
    private final transient String name;

    /**
     * Ctor.
     * @param script Script name
     */
    Script(final String script) {
        this.name = script;
    }

    /**
     * Execute.
     * @param xml Talk xml
     * @return Exit code
     * @throws IOException If fails
     */
    public int exec(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        new Shell.Safe(shell).exec(
            String.format(
                "cd %s && cat > %s && chmod a+x %1$s",
                SSH.escape(dir), SSH.escape(this.name)
            ),
            this.getClass().getResourceAsStream(this.name),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
        return new Shell.Empty(shell).exec(
            new JoinedText(
                " && ",
                "set -o pipefail",
                String.format("cd %s", SSH.escape(dir)),
                String.format(
                    "/bin/bash %s >> stdout 2>&1",
                    SSH.escape(this.name)
                )
            ).asString()
        );
    }

}
