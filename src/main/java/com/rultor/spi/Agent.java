/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Arrays;
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
                Logger.error(this, "%[exception]s", ex);
            }
        }
    }
}
