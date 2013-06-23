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
package com.rultor.conveyer;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Instance;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.User;
import com.rultor.spi.Work;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple Instantary, mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.DEBUG)
@ToString
@EqualsAndHashCode(of = { "instances", "repo" })
final class SimpleInstantary implements Instantary {

    /**
     * All instances.
     * @checkstyle LineLength (2 lines)
     */
    private final transient ConcurrentMap<String, SimpleInstantary.SpecdInstance> instances =
        new ConcurrentHashMap<String, SimpleInstantary.SpecdInstance>(0);

    /**
     * Repository.
     */
    private final transient Repo repo;

    /**
     * Public ctor.
     * @param rep Repo
     */
    protected SimpleInstantary(final Repo rep) {
        this.repo = rep;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Instance get(final User user, final String name, final Spec spec)
        throws Repo.InstantiationException {
        synchronized (this.instances) {
            final String key = String.format("%s %s", user.urn(), name);
            SpecdInstance instance = this.instances.get(key);
            if (instance == null || !instance.same(spec)) {
                instance = new SpecdInstance(this.repo.make(user, spec), spec);
            }
            this.instances.put(key, instance);
            return instance;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final MetricRegistry registry) {
        registry.register(
            MetricRegistry.name(this.getClass(), "instances-total"),
            new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return SimpleInstantary.this.instances.size();
                }
            }
        );
    }

    /**
     * Specd instance.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "origin", "spec" })
    private static final class SpecdInstance implements Instance {
        /**
         * Original instance.
         */
        private final transient Instance origin;
        /**
         * Spec.
         */
        private final transient Spec spec;
        /**
         * Public ctor.
         * @param instance Original one
         * @param txt Spec
         */
        protected SpecdInstance(final Instance instance, final Spec txt) {
            this.origin = instance;
            this.spec = txt;
        }
        /**
         * Is it in the same spec?
         * @param copy Copy of the spec
         * @return TRUE if encapsulated spec is identical to the one provided
         */
        public boolean same(final Spec copy) {
            return this.spec.equals(copy);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @Loggable(value = Loggable.DEBUG, limit = 1, unit = TimeUnit.HOURS)
        public void pulse(final Work work) throws Exception {
            this.origin.pulse(work);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.origin.toString();
        }
    }

}
