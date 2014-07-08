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
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.Shell;
import com.rultor.agents.shells.TalkShells;
import java.io.IOException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Marks the daemon as done.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class EndsDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public EndsDaemon() {
        super("/talk/daemon[started and not(code) and not(ended)]");
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        final int exit = new Shell.Empty(shell).exec(
            StringUtils.join(
                String.format("dir=%s", dir),
                " && if [ ! -e ${dir}/pid ]; then exit 1; fi",
                " && pid=$(cat ${dir}/pid)",
                " && ps -p $pid >/dev/null"
            )
        );
        final Directives dirs = new Directives();
        if (exit == 0) {
            Logger.info(this, "the daemon is still running in %s", dir);
        } else {
            dirs.append(this.end(shell, dir));
        }
        return dirs;
    }

    /**
     * End this daemon.
     * @param shell Shell
     * @param dir The dir
     * @return Directives
     * @throws IOException If fails
     */
    private Iterable<Directive> end(final Shell shell,
        final String dir) throws IOException {
        final int exit = new Shell.Empty(shell).exec(
            String.format("grep RULTOR-SUCCESS %s/stdout", dir)
        );
        Logger.info(this, "daemon finished at %s, exit: %d", dir, exit);
        return new Directives().xpath("/talk/daemon")
            .add("ended")
            .set(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date()))
            .up()
            .add("code").set(Integer.toString(exit));
    }

}
