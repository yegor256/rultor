/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Super Agent.
 *
 * @since 1.0
 */
@Immutable
public interface SuperAgent {

    /**
     * Execute it.
     * @param talks All talks
     * @throws IOException If fails
     */
    void execute(Talks talks) throws IOException;

    /**
     * Iterative.
     *
     * @since 1.0
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "children")
    final class Iterative implements SuperAgent {
        /**
         * Agents to run.
         */
        private final transient Array<SuperAgent> children;

        /**
         * Ctor.
         * @param list List of them
         */
        public Iterative(final Iterable<SuperAgent> list) {
            this.children = new Array<>(list);
        }

        @Override
        public void execute(final Talks talks) throws IOException {
            for (final SuperAgent agent : this.children) {
                agent.execute(talks);
            }
        }
    }

    /**
     * Disabled.
     *
     * @since 1.0
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "agent")
    final class Disabled implements SuperAgent {
        /**
         * Agent to disable.
         */
        private final transient SuperAgent agent;

        /**
         * Ctor.
         * @param agt The agent
         */
        public Disabled(final SuperAgent agt) {
            this.agent = agt;
        }

        @Override
        public void execute(final Talks talks) throws IOException {
            // do nothing
        }
    }

    /**
     * Quiet.
     *
     * @since 1.0
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "agent")
    final class Quiet implements SuperAgent {
        /**
         * Agent to disable.
         */
        private final transient SuperAgent agent;

        /**
         * Ctor.
         * @param agt The agent
         */
        public Quiet(final SuperAgent agt) {
            this.agent = agt;
        }

        @Override
        @SuppressWarnings("PMD.AvoidCatchingGenericException")
        public void execute(final Talks talks) throws IOException {
            try {
                this.agent.execute(talks);
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception ex) {
                Logger.error(this, "%[exception]s", ex);
            }
        }
    }

}
