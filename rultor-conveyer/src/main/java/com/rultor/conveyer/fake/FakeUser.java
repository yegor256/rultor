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
package com.rultor.conveyer.fake;

import com.jcabi.urn.URN;
import com.rultor.spi.Account;
import com.rultor.spi.Rule;
import com.rultor.spi.Rules;
import com.rultor.spi.Sheet;
import com.rultor.spi.Spec;
import com.rultor.spi.Stands;
import com.rultor.spi.User;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Dollars;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Fake users that always return one rule.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.CyclomaticComplexity" })
final class FakeUser implements User {

    /**
     * Work to return.
     */
    private final transient Work work;

    /**
     * Spec to use.
     */
    private final transient Spec specification;

    /**
     * Public ctor.
     * @param wrk Work
     * @param spc Spec
     */
    protected FakeUser(final Work wrk, final Spec spc) {
        this.work = wrk;
        this.specification = spc;
    }

    @Override
    public Rules rules() {
        // @checkstyle AnonInnerLength (50 lines)
        return new Rules() {
            @Override
            public Rule get(final String name) {
                return new Rule() {
                    @Override
                    public void update(final Spec spc) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public String name() {
                        return name;
                    }
                    @Override
                    public Wallet wallet(final Work wrk, final URN urn,
                        final String rule) {
                        return new Wallet() {
                            @Override
                            public void charge(final String details,
                                final Dollars amount) {
                                throw new UnsupportedOperationException();
                            }
                            @Override
                            public Wallet delegate(final URN urn,
                                final String rule) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                    @Override
                    public Spec spec() {
                        return FakeUser.this.specification;
                    }
                };
            }
            @Override
            public void remove(final String name) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void create(final String name) {
                throw new UnsupportedOperationException();
            }
            @Override
            public Iterator<Rule> iterator() {
                throw new UnsupportedOperationException();
            }
            @Override
            public boolean contains(final String name) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN urn() {
        return FakeUser.this.work.owner();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stands stands() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account account() {
        return new Account() {
            @Override
            public Dollars balance() {
                return new Dollars(1);
            }
            @Override
            public Sheet sheet() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void fund(final Dollars amount, final String details) {
                throw new UnsupportedOperationException();
            }
        };
    }

}
