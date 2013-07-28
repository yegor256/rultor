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
package com.rultor.shell;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Relics after shell execution.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "origin", "all" })
@Loggable(Loggable.DEBUG)
public final class Relics implements Shells {

    /**
     * Original shells.
     */
    private final transient Shells origin;

    /**
     * Relics to collect.
     */
    private final transient Array<Relic> all;

    /**
     * Public ctor.
     * @param shells Original shells
     * @param rlcs Relics to collect
     */
    public Relics(
        @NotNull(message = "shells can't be NULL") final Shells shells,
        @NotNull(message = "relics can't be NULL")
        final Collection<Relic> rlcs) {
        this.origin = shells;
        this.all = new Array<Relic>(rlcs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s with %d relic(s)",
            this.origin,
            this.all.size()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shell acquire() throws IOException {
        final Shell shell = this.origin.acquire();
        return new Shell() {
            @Override
            public int exec(final String command, final InputStream stdin,
                final OutputStream stdout, final OutputStream stderr)
                throws IOException {
                final int code = shell.exec(command, stdin, stdout, stderr);
                final PrintWriter writer = new PrintWriter(stdout, true);
                for (Relic relic : Relics.this.all) {
                    writer.println(Resonant.encode(relic.discover(shell)));
                }
                writer.close();
                return code;
            }
            @Override
            public void close() throws IOException {
                shell.close();
            }
        };
    }

}
