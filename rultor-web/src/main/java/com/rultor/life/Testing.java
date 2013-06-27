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
package com.rultor.life;

import com.codahale.metrics.MetricRegistry;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rultor.repo.ClasspathRepo;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulse;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Testing profile.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode
@Loggable(Loggable.INFO)
@SuppressWarnings("PMD.TooManyMethods")
final class Testing implements Profile {

    /**
     * {@inheritDoc}
     */
    @Override
    public Repo repo() {
        return new ClasspathRepo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Users users() {
        // @checkstyle AnonInnerLength (100 lines)
        return new Users() {
            private final ConcurrentMap<URN, User> all =
                new ConcurrentHashMap<URN, User>(0);
            @Override
            public Collection<User> everybody() {
                return this.all.values();
            }
            @Override
            public User fetch(final URN urn) {
                this.all.putIfAbsent(
                    urn,
                    new User() {
                        private final ConcurrentMap<String, Unit> mine =
                            new ConcurrentHashMap<String, Unit>(0);
                        @Override
                        public URN urn() {
                            return urn;
                        }
                        @Override
                        public Map<String, Unit> units() {
                            return this.mine;
                        }
                        @Override
                        public Unit create(final String name) {
                            this.mine.putIfAbsent(
                                name, new Testing.MemoryUnit(name)
                            );
                            return this.mine.get(name);
                        }
                        @Override
                        public void remove(final String name) {
                            this.mine.remove(name);
                        }
                    }
                );
                return this.all.get(urn);
            }
            @Override
            public void register(final MetricRegistry registry) {
                assert registry != null;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue queue() {
        return new Queue.Memory();
    }

    /**
     * In-memory unit.
     */
    @Immutable
    private static final class MemoryUnit implements Unit {
        /**
         * All specs.
         */
        private static final ConcurrentMap<String, Spec> SPECS =
            new ConcurrentHashMap<String, Spec>(0);
        /**
         * Name of the unit.
         */
        private final transient String name;
        /**
         * Public ctor.
         * @param unit Name of it
         */
        protected MemoryUnit(final String unit) {
            this.name = unit;
            Testing.MemoryUnit.SPECS.put(this.name, new Spec.Simple());
        }
        @Override
        public SortedMap<Date, Pulse> pulses() {
            return new ConcurrentSkipListMap<Date, Pulse>();
        }
        @Override
        public void spec(final Spec spec) {
            Testing.MemoryUnit.SPECS.put(this.name, spec);
        }
        @Override
        public Spec spec() {
            return Testing.MemoryUnit.SPECS.get(this.name);
        }
        @Override
        public Drain drain() {
            return new Drain() {
                @Override
                public void push(final Work work, final Drain.Line line) {
                    assert true;
                }
                @Override
                public void close() throws IOException {
                    assert true;
                }
            };
        }
    }

}
