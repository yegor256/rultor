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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rultor.repo.ClasspathRepo;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
     * All units.
     */
    private static final ConcurrentMap<String, Unit> UNITS =
        new ConcurrentHashMap<String, Unit>(0);

    /**
     * All specs.
     */
    private static final ConcurrentMap<String, Spec> SPECS =
        new ConcurrentHashMap<String, Spec>(0);

    /**
     * All drains.
     */
    private static final ConcurrentMap<String, Spec> DRAINS =
        new ConcurrentHashMap<String, Spec>(0);

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
            @Override
            public Map<URN, User> everybody() {
                return new AbstractMap<URN, User>() {
                    @Override
                    public Set<Map.Entry<URN, User>> entrySet() {
                        return new TreeSet<Map.Entry<URN, User>>();
                    }
                    @Override
                    public User get(final Object urn) {
                        return new Testing.MemoryUser(
                            URN.create(urn.toString())
                        );
                    }
                };
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
     * In-memory user.
     */
    @Immutable
    private static final class MemoryUser implements User {
        /**
         * URN of the user.
         */
        private final transient URN name;
        /**
         * Public ctor.
         * @param urn Name of it
         */
        protected MemoryUser(final URN urn) {
            this.name = urn;
        }
        @Override
        public URN urn() {
            return URN.create(this.name.toString());
        }
        @Override
        public Map<String, Unit> units() {
            return new AbstractMap<String, Unit>() {
                @Override
                public Set<Map.Entry<String, Unit>> entrySet() {
                    return Testing.UNITS.entrySet();
                }
                @Override
                public Unit get(final Object unit) {
                    return new Testing.MemoryUnit(unit.toString());
                }
                @Override
                public Unit put(final String txt, final Unit unit) {
                    Testing.SPECS.put(txt, new Spec.Simple());
                    Testing.DRAINS.put(txt, new Spec.Simple());
                    return Testing.UNITS.put(txt, unit);
                }
            };
        }
    }

    /**
     * In-memory unit.
     */
    @Immutable
    private static final class MemoryUnit implements Unit {
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
        }
        @Override
        public void update(final Spec spec, final Spec drain) {
            Testing.SPECS.put(this.name, spec);
            Testing.DRAINS.put(this.name, drain);
        }
        @Override
        public Spec spec() {
            return Testing.SPECS.get(this.name);
        }
        @Override
        public Spec drain() {
            return Testing.DRAINS.get(this.name);
        }
    }

}
