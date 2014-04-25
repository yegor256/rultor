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

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.io.OutputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * ASCII command codes aware output stream.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = "origin")
@Loggable(Loggable.DEBUG)
public final class ASCIIOutputStream extends OutputStream {

    /**
     * Original stream.
     */
    private final transient OutputStream origin;

    /**
     * Accumulated latest line.
     */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final transient StringBuffer line = new StringBuffer();

    /**
     * Public ctor.
     * @param stream Original stream
     */
    public ASCIIOutputStream(
        @NotNull(message = "stream can't be NULL") final OutputStream stream) {
        super();
        this.origin = stream;
    }

    @Override
    public void write(final int chr) throws IOException {
        if (chr == '\015') {
            this.line.setLength(0);
        } else if (chr == '\010') {
            synchronized (this.line) {
                if (this.line.length() > 0) {
                    this.line.setLength(this.line.length() - 1);
                }
            }
        } else if (chr == '\011') {
            final int lag = Tv.EIGHT - this.line.length() % Tv.EIGHT;
            for (int space = 0; space < lag; ++space) {
                this.line.append(' ');
            }
        } else if (chr == '\012') {
            synchronized (this.line) {
                this.origin.write(
                    this.line.toString().getBytes(CharEncoding.UTF_8)
                );
                this.origin.write(chr);
                this.line.setLength(0);
            }
        } else {
            this.line.append((char) chr);
        }
    }

}
