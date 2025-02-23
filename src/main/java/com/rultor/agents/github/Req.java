/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
     *
     * @since 1.3
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
