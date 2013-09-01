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
import com.jcabi.immutable.Array;
import com.jcabi.urn.URN;
import com.rultor.spi.Arguments;
import com.rultor.spi.SpecException;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Wallet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
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
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "grammar", "owner", "name" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
final class RefForeign implements Variable<Object> {

    /**
     * Grammar where to look for vars.
     */
    private final transient Grammar grammar;

    /**
     * Owner of the unit (who provides the unit).
     */
    private final transient URN owner;

    /**
     * The name.
     */
    private final transient String name;

    /**
     * Parameters.
     */
    private final transient Array<Variable<?>> children;

    /**
     * Public ctor.
     * @param grm Grammar to use
     * @param urn Owner of the unit
     * @param ref RefForeign
     * @param childs Enclosed child parameters
     * @checkstyle ParameterNumber (4 lines)
     */
    protected RefForeign(final Grammar grm, final URN urn,
        final String ref, final Collection<Variable<?>> childs) {
        Validate.matchesPattern(ref, "[-_\\w]+");
        this.grammar = grm;
        this.owner = urn;
        this.name = ref;
        this.children = new Array<Variable<?>>(childs);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (8 lines)
     */
    @Override
    @NotNull
    public Object instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "arguments can't be NULL") final Arguments args)
        throws SpecException {
        final User user = users.get(this.owner);
        try {
            return this.alter(
                this.grammar.parse(
                    user.urn(), user.rules().get(this.name).spec().asText()
                ).instantiate(users, this.mapping(users, args)),
                args
            );
        } catch (SpecException ex) {
            throw new SpecException(
                String.format(
                    "failed to instantiate %s:%s with %d params",
                    this.owner, this.name, this.children.size()
                ),
                ex
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asText() {
        return new StringBuilder()
            .append(this.owner)
            .append(':')
            .append(this.name)
            .append('(')
            .append(new Brackets<Variable<?>>(this.children))
            .append(')')
            .toString();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Map<Integer, String> arguments() throws SpecException {
        final ConcurrentMap<Integer, String> args =
            new ConcurrentSkipListMap<Integer, String>();
        for (Variable<?> var : this.children) {
            args.putAll(var.arguments());
        }
        return args;
    }

    /**
     * Make arguments for the underlying spec.
     * @param users Users to use for instantiation
     * @param args Arguments received from the upper level caller
     * @return Arguments to use
     * @throws SpecException If fails
     * @checkstyle RedundantThrows (5 lines)
     */
    private Arguments mapping(final Users users, final Arguments args)
        throws SpecException {
        final Collection<Object> values =
            new ArrayList<Object>(this.children.size());
        for (Variable<?> var : this.children) {
            values.add(var.instantiate(users, args));
        }
        try {
            return new Arguments(
                args.work(),
                args.wallet().delegate(this.owner, this.name),
                values
            );
        } catch (Wallet.NotEnoughFundsException ex) {
            throw new SpecException(ex);
        }
    }

    /**
     * Alter the object by injecting name into it.
     * @param object The object
     * @param args Arguments used to instantiate it
     * @return Altered object
     * @throws SpecException If some error inside
     * @checkstyle RedundantThrows (5 lines)
     */
    private Object alter(final Object object, final Arguments args)
        throws SpecException {
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(Composite.METHOD)) {
                try {
                    method.invoke(
                        object,
                        String.format(
                            "`%s:%s` with %s", this.owner, this.name, args
                        )
                    );
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

}
