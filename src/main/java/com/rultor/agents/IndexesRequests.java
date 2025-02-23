/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.List;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.number.MaxOf;
import org.cactoos.number.NumberOf;
import org.xembly.Directives;

/**
 * Adds index to all the requests received.
 *
 * @since 1.0
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
     * @checkstyle NonStaticMethodCheck (15 lines)
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private int index(final Talk talk) throws IOException {
        final List<Number> indexes = new ListOf<>(
            new Mapped<>(
                NumberOf::new,
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
