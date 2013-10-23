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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.rultor.spi.Arguments;
import com.rultor.spi.Proxy;
import com.rultor.spi.SpecException;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.tools.Exceptions;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Composite.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "type", "vars" })
@Loggable(Loggable.DEBUG)
final class Composite implements Variable<Object> {

    /**
     * Type name.
     */
    private final transient String type;

    /**
     * Type name.
     */
    private final transient Array<Variable<?>> vars;

    /**
     * Public ctor.
     * @param name Name of type
     * @param args Arguments
     */
    Composite(final String name, final Collection<Variable<?>> args) {
        this.type = name;
        this.vars = new Array<Variable<?>>(args);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (10 lines)
     */
    @Override
    @NotNull
    public Object instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "arguments can't be NULL") final Arguments arguments)
        throws SpecException {
        final Object[] args = new Object[this.vars.size()];
        final Class<?>[] types = new Class<?>[this.vars.size()];
        final int size = this.vars.size();
        for (int idx = 0; idx < size; ++idx) {
            final Object object = this.vars.get(idx)
                .instantiate(users, arguments);
            args[idx] = object;
            types[idx] = object.getClass();
        }
        final Constructor<?> ctor = this.ctor(types);
        Object object;
        try {
            object = ctor.newInstance(args);
        } catch (InstantiationException ex) {
            throw new SpecException(
                String.format(
                    "failed to instantiate using \"%s\" constructor: %s",
                    ctor,
                    Exceptions.message(ex)
                ),
                ex
            );
        } catch (IllegalAccessException ex) {
            throw new SpecException(
                String.format(
                    "failed to access \"%s\": %s",
                    ctor,
                    Exceptions.message(ex)
                ),
                ex
            );
        } catch (InvocationTargetException ex) {
            throw new SpecException(
                String.format(
                    "failed to invoke \"%s\": %s",
                    ctor,
                    Exceptions.message(ex)
                ),
                ex
            );
        }
        if (object instanceof Proxy) {
            object = Proxy.class.cast(object).object();
        }
        return object;
    }

    @Override
    public String asText() {
        return new StringBuilder(0)
            .append(this.type)
            .append('(')
            .append(new Brackets<Variable<?>>(this.vars))
            .append(')')
            .toString();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Map<Integer, String> arguments() throws SpecException {
        final ImmutableMap.Builder<Integer, String> args =
            new ImmutableMap.Builder<Integer, String>();
        for (final Variable<?> var : this.vars) {
            args.putAll(var.arguments());
        }
        return args.build();
    }

    /**
     * Find the best matching constructor.
     * @param types Types
     * @return The ctor
     * @throws SpecException If can't get it
     * @checkstyle RedundantThrows (5 lines)
     */
    private Constructor<?> ctor(final Class<?>... types)
        throws SpecException {
        final Class<?> cls;
        try {
            cls = Class.forName(this.type);
        } catch (ClassNotFoundException ex) {
            throw new SpecException(ex);
        }
        Constructor<?> ctor = null;
        for (final Constructor<?> opt : cls.getConstructors()) {
            if (Composite.isInherited(opt.getParameterTypes(), types)) {
                ctor = opt;
                break;
            }
        }
        if (ctor == null) {
            throw new SpecException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "can't find constructor %s%s, available alternatives are: %s",
                    this.type,
                    Arrays.asList(types),
                    Arrays.asList(cls.getConstructors())
                )
            );
        }
        return ctor;
    }

    /**
     * Right set of types inherits types from the left.
     * @param parents Supposedly parent types
     * @param kids Child types
     * @return TRUE if they match
     */
    private static boolean isInherited(final Class<?>[] parents,
        final Class<?>[] kids) {
        boolean match;
        if (parents.length == kids.length) {
            match = true;
            for (int idx = 0; idx < parents.length; ++idx) {
                if (!Composite.box(parents[idx])
                    .isAssignableFrom(Composite.box(kids[idx]))) {
                    match = false;
                    break;
                }
            }
        } else {
            match = false;
        }
        return match;
    }

    /**
     * Auto-box the type.
     * @param origin Type to auto-box
     * @return Non-scalar type
     */
    private static Class<?> box(final Class<?> origin) {
        final Class<?> out;
        if (int.class.equals(origin)) {
            out = Integer.class;
        } else if (long.class.equals(origin)) {
            out = Long.class;
        } else if (double.class.equals(origin)) {
            out = Double.class;
        } else if (boolean.class.equals(origin)) {
            out = Boolean.class;
        } else {
            out = origin;
        }
        return out;
    }

}
