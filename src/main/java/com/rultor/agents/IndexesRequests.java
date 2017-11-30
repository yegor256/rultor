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
package com.rultor.agents;

import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import org.cactoos.collection.Mapped;
import org.cactoos.list.SolidList;
import org.cactoos.scalar.MaxOf;
import org.cactoos.scalar.NumberOf;
import org.xembly.Directives;

/**
 * Adds index to all the requests received.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class IndexesRequests implements SuperAgent {
    @Override
    public void execute(final Talks talks) throws IOException {
        int idx = this.index(talks);
        for (final Talk talk : talks.active()) {
            idx += 1;
            talk.modify(
                new Directives()
                    .xpath("/talk/request")
                    .attr("index", Integer.toString(idx))
            );
        }
    }

    /**
     * Calculates maximal index value for a {@link Talks} object.
     * @param talks The {@link Talks} object
     * @return The maximal index value
     * @throws IOException if the content of one {@link Talk} object can't be read
     */
    private int index(final Talks talks) throws IOException {
        int index = 0;
        for (final Talk talk : talks.active()) {
            final int idx = this.index(talk);
            if (idx > index) {
                index = idx;
            }
        }
        return index;
    }

    /**
     * Calculates maximal (existing) index value of a {@link Talk} object.
     * @param talk The {@link Talk} object
     * @return The maximal index value
     * @throws IOException if the content of the {@link Talk} object can't be
     *  read
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private int index(final Talk talk) throws IOException {
        final SolidList<Number> indexes = new SolidList<>(
            new Mapped<>(
                input -> new NumberOf(input),
                talk.read()
                    .xpath("/talk/archive/log/@index|/talk/request/@index")
            )
        );
        final int index;
        if (indexes.iterator().hasNext()) {
            index = new MaxOf(indexes).intValue();
        } else {
            index = 0;
        }
        return index;
    }
}
