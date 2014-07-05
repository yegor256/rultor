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
package com.rultor.agents;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.Array;
import com.jcabi.xml.XML;
import com.rultor.spi.Repo;
import com.rultor.spi.Talk;
import java.io.IOException;

/**
 * Agent for a single talk.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface TalkAgent {

    /**
     * Execute it.
     * @param talk Talk to work with
     */
    void execute(Talk talk) throws IOException;

    /**
     * Wrap.
     */
    @Immutable
    final class Wrap implements Agent {
        /**
         * Encapsulated agent.
         */
        private final transient TalkAgent origin;
        /**
         * Ctor.
         * @param agent Original
         */
        public Wrap(final TalkAgent agent) {
            this.origin = agent;
        }
        @Override
        public void execute(final Repo repo) throws IOException {
            for (final Talk talk : repo.talks().iterate()) {
                this.origin.execute(talk);
            }
        }
    }

    /**
     * Abstract.
     */
    @Immutable
    abstract class Abstract implements TalkAgent {
        /**
         * Encapsulated XPaths.
         */
        private final transient Array<String> xpaths;
        /**
         * Ctor.
         * @param args XPath expressions
         */
        public Abstract(final String... args) {
            this.xpaths = new Array<String>(args);
        }
        @Override
        public void execute(final Talk talk) throws IOException {
            final XML xml = talk.read();
            boolean good = true;
            for (final String xpath : this.xpaths) {
                if (xml.nodes(xpath).isEmpty()) {
                    good = false;
                    break;
                }
            }
            if (good) {
                this.process(talk, xml);
            }
        }
        /**
         * Process it.
         * @param talk The talk
         * @param xml Its xml
         * @throws IOException If fails
         */
        protected abstract void process(Talk talk, XML xml) throws IOException;
    }

}
