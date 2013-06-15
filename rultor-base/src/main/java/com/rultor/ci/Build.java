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
package com.rultor.ci;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.board.Billboard;
import com.rultor.shell.Shell;
import com.rultor.shell.Shells;
import com.rultor.spi.Pulseable;
import com.rultor.spi.State;
import com.rultor.spi.Work;
import java.io.ByteArrayOutputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * Build.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "shells", "script", "board" })
@Loggable(Loggable.DEBUG)
public final class Build implements Pulseable {

    /**
     * Shells.
     */
    private final transient Shells shells;

    /**
     * Script to execute.
     */
    private final transient String script;

    /**
     * Where to notify about success/failure.
     */
    private final transient Billboard board;

    /**
     * Public ctor.
     * @param shls Shells
     * @param scrt Script to run there
     * @param brd The board where to announce
     */
    public Build(final Shells shls, final String scrt,
        final Billboard brd) {
        this.shells = shls;
        this.script = scrt;
        this.board = brd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pulse(@NotNull final Work work, @NotNull final State state) {
        final Shell shell = this.shells.acquire();
        int code;
        try {
            code = shell.exec(
                this.script,
                IOUtils.toInputStream(""),
                new ByteArrayOutputStream(),
                new ByteArrayOutputStream()
            );
        } finally {
            IOUtils.closeQuietly(shell);
        }
        if (code == 0) {
            this.board.announce(
                String.format(
                    "%s completed successfully",
                    work.unit()
                )
            );
        } else {
            this.board.announce(
                String.format(
                    "%s failed",
                    work.unit()
                )
            );
        }
    }

}
