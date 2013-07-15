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
package com.rultor.repo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rultor.spi.Dollars;
import com.rultor.spi.Receipt;
import com.rultor.spi.Spec;
import com.rultor.spi.SpecException;
import com.rultor.spi.Time;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Work;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * RefForeign.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "grammar", "owner", "name" })
@Loggable(Loggable.DEBUG)
final class RefForeign implements Variable<Object> {

    /**
     * Grammar where to look for vars.
     */
    private final transient Grammar grammar;

    /**
     * Client of the unit (who is using the unit).
     */
    private final transient URN client;

    /**
     * Owner of the unit (who provides the unit).
     */
    private final transient URN owner;

    /**
     * The name.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param grm Grammar to use
     * @param clnt Client of the unit
     * @param urn Owner of the unit
     * @param ref RefForeign
     * @checkstyle ParameterNumber (4 lines)
     */
    protected RefForeign(final Grammar grm, final URN clnt,
        final URN urn, final String ref) {
        Validate.matchesPattern(ref, "[-_\\w]+");
        this.grammar = grm;
        this.client = clnt;
        this.owner = urn;
        this.name = ref;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (8 lines)
     */
    @Override
    @NotNull
    public Object instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "work can't be NULL") final Work work)
        throws SpecException {
        final User user = users.get(this.owner);
        if (!user.units().contains(this.name)) {
            throw new SpecException(
                String.format(
                    "unit '%s' not found in '%s'",
                    this.name, this.owner
                )
            );
        }
        final Work monetary;
        if (this.client.equals(this.owner)) {
            monetary = work;
        } else {
            monetary = new RefForeign.Monetary(users, work);
        }
        return this.alter(
            this.grammar.parse(
                user.urn(), user.get(this.name).spec().asText()
            ).instantiate(users, monetary)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asText() {
        return String.format("%s:%s", this.owner, this.name);
    }

    /**
     * Alter the object by injecting name into it.
     * @param object The object
     * @return Altered object
     * @throws SpecException If some error inside
     * @checkstyle RedundantThrows (5 lines)
     */
    private Object alter(final Object object)
        throws SpecException {
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(Composite.METHOD)) {
                try {
                    method.invoke(object, String.format("`%s`", this.name));
                } catch (IllegalAccessException ex) {
                    throw new SpecException(ex);
                } catch (SecurityException ex) {
                    throw new SpecException(ex);
                } catch (InvocationTargetException ex) {
                    throw new SpecException(ex);
                }
            }
        }
        return object;
    }

    private final class Monetary implements Work {
        /**
         * Users.
         */
        private final transient Users users;
        /**
         * Original work.
         */
        private final transient Work origin;
        /**
         * Public ctor.
         * @param usrs Users
         * @param wrk Origin work
         */
        protected Monetary(final Users usrs, final Work wrk) {
            this.users = usrs;
            this.origin = wrk;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Time started() {
            return this.origin.started();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public URN owner() {
            return this.origin.owner();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String unit() {
            return this.origin.unit();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Spec spec() {
            return this.origin.spec();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void charge(final String details, final Dollars amount) {
            this.users.charge(
                new Receipt.Simple(
                    new Time(),
                    RefForeign.this.client,
                    RefForeign.this.owner,
                    details,
                    amount,
                    this.unit()
                )
            );
        }
    }

}
