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
package com.rultor.board;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * All variables are compacted.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "maximum", "board" })
@Loggable(Loggable.DEBUG)
public final class Compact implements Billboard {

    /**
     * Maximum number of characters in every item.
     */
    private final transient int maximum;

    /**
     * Original board.
     */
    private final transient Billboard board;

    /**
     * Public ctor.
     * @param max Maximum
     * @param brd Original board
     */
    public Compact(final int max, @NotNull(message = "board can't be NULL")
        final Billboard brd) {
        Validate.isTrue(
            max > Tv.FIFTEEN,
            "Maximum length should be more than 15"
        );
        this.maximum = max;
        this.board = brd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s with data of %d maximum length",
            this.board, this.maximum
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(@NotNull final Announcement anmt) throws IOException {
        final ImmutableMap.Builder<String, Object> args =
            new ImmutableMap.Builder<String, Object>();
        for (Map.Entry<String, Object> entry : anmt.args().entrySet()) {
            args.put(
                entry.getKey(),
                this.compress(entry.getValue().toString())
            );
        }
        this.board.announce(new Announcement(anmt.level(), args.build()));
    }

    /**
     * Compressed variant of the text.
     * @param text The text to compress
     * @return Compressed
     */
    private String compress(final String text) {
        final String output;
        if (text.length() > this.maximum) {
            final int visible = (this.maximum - Tv.FIFTEEN) / 2;
            output = new StringBuilder()
                .append(text.substring(0, visible))
                .append(".. ")
                .append(text.length() - visible * 2)
                .append(" skipped ..")
                .append(text.substring(text.length() - visible))
                .toString();
        } else {
            output = text;
        }
        return output;
    }

}
