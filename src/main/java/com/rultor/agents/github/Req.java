/**
 * Copyright (c) 2009-2017, rultor.com
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
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.ArrayMap;
import java.util.Collections;
import java.util.Map;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Request.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3
 */
@Immutable
public interface Req {

    /**
     * Empty, nothing found.
     */
    Req EMPTY = new Req() {
        @Override
        public Iterable<Directive> dirs() {
            return Collections.emptyList();
        }
    };

    /**
     * Come back later to the same question.
     */
    Req LATER = new Req() {
        @Override
        public Iterable<Directive> dirs() {
            return Collections.emptyList();
        }
    };

    /**
     * Done, but nothing special.
     */
    Req DONE = new Req() {
        @Override
        public Iterable<Directive> dirs() {
            return Collections.emptyList();
        }
    };

    /**
     * Directives.
     * @return Dirs
     */
    Iterable<Directive> dirs();

    /**
     * Simple impl.
     */
    @Immutable
    final class Simple implements Req {
        /**
         * Type.
         */
        private final transient String type;
        /**
         * Map of args.
         */
        private final transient ArrayMap<String, String> map;
        /**
         * Ctor.
         * @param tpe Type
         * @param args Args
         */
        public Simple(final String tpe, final Map<String, String> args) {
            this.type = tpe;
            this.map = new ArrayMap<>(args);
        }
        @Override
        public Iterable<Directive> dirs() {
            final Directives dirs = new Directives()
                .addIf("type").set(this.type).up().addIf("args");
            for (final Map.Entry<String, String> ent : this.map.entrySet()) {
                dirs.add("arg")
                    .attr("name", ent.getKey())
                    .set(ent.getValue())
                    .up();
            }
            return dirs.up();
        }
    }

}
