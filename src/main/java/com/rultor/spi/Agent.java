/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Agent.
 *
 * @since 1.0
 */
@Immutable
public interface Agent {

    /**
     * Execute it.
     * @param talk Talk to work with
     * @throws IOException If fails
     */
    void execute(Talk talk) throws IOException;

    /**
     * Iterative.
     *
     * @since 1.0
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "children")
    final class Iterative implements Agent {
        /**
         * Agents to run.
         */
        private final transient Array<Agent> children;

        /**
         * Ctor.
         * @param list List of them
         */
        public Iterative(final Agent... list) {
            this(Arrays.asList(list));
        }

        /**
         * Ctor.
         * @param list List of them
         */
        public Iterative(final Iterable<Agent> list) {
            this.children = new Array<>(list);
        }

        @Override
        public void execute(final Talk talk) throws IOException {
            int total = 0;
            for (final Agent agent : this.children) {
                agent.execute(talk);
                ++total;
            }
            Logger.debug(this, "Executed %d agent(s)", total);
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
    final class Disabled implements Agent {
        /**
         * Agent to disable.
         */
        private final transient Agent agent;

        /**
         * Disable it?
         */
        private final transient boolean disable;

        /**
         * Ctor.
         * @param agt Agent
         */
        public Disabled(final Agent agt) {
            this(agt, true);
        }

        /**
         * Ctor.
         * @param agt Agent
         * @param dsbl Disable it?
         */
        public Disabled(final Agent agt, final boolean dsbl) {
            this.agent = agt;
            this.disable = dsbl;
        }

        @Override
        public void execute(final Talk talk) throws IOException {
            if (!this.disable) {
                this.agent.execute(talk);
            }
        }
    }

    /**
     * Swallows all exceptions.
     *
     * @since 1.0
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "agent")
    final class Quiet implements Agent {
        /**
         * Agent to defend.
         */
        private final transient Agent agent;

        /**
         * Ctor.
         * @param agt Agent
         */
        public Quiet(final Agent agt) {
            this.agent = agt;
        }

        @Override
        @SuppressWarnings("PMD.AvoidCatchingGenericException")
        public void execute(final Talk talk) throws IOException {
            try {
                this.agent.execute(talk);
            // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception ex) {
                Logger.error(
                    this, "In %s:%d %[exception]s",
                    talk.name(), talk.number(), ex
                );
            }
        }
    }

    /**
     * Only if the name of the talk DOESN'T match the regular expression.
     *
     * @since 1.0
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = {"agent", "pattern"})
    final class SkipIfName implements Agent {
        /**
         * Agent to defend.
         */
        private final transient Agent agent;

        /**
         * The regular expression.
         */
        private final transient Pattern pattern;

        /**
         * Ctor.
         * @param agt Agent
         * @param ptn Pattern to match
         */
        public SkipIfName(final Agent agt, final String ptn) {
            this.agent = agt;
            this.pattern = Pattern.compile(ptn);
        }

        @Override
        public void execute(final Talk talk) throws IOException {
            final String name = talk.read().xpath("/talk/@name").get(0);
            if (!this.pattern.matcher(name).matches()) {
                this.agent.execute(talk);
            }
        }
    }
}
