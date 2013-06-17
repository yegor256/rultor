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
import com.jcabi.aspects.Tv;
import com.rultor.spi.Repo;
import com.rultor.spi.User;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Composite.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "type", "vars" })
@Loggable(Loggable.DEBUG)
final class Composite implements Variable<Object> {

    /**
     * Indentation.
     */
    private static final String INDENT = "  ";

    /**
     * EOL.
     */
    private static final String EOL = "\n";

    /**
     * Type name.
     */
    private final transient String type;

    /**
     * Type name.
     */
    private final transient Variable[] vars;

    /**
     * Public ctor.
     * @param name Name of type
     * @param args Arguments
     */
    protected Composite(final String name, final Collection<Variable<?>> args) {
        this.type = name;
        this.vars = args.toArray(new Variable<?>[args.size()]);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Object instantiate(final User user)
        throws Repo.InstantiationException {
        final Object[] args = new Object[this.vars.length];
        final Class<?>[] types = new Class<?>[this.vars.length];
        for (int idx = 0; idx < this.vars.length; ++idx) {
            final Object object = this.vars[idx].instantiate(user);
            args[idx] = object;
            types[idx] = object.getClass();
        }
        try {
            return this.ctor(types).newInstance(args);
        } catch (ClassNotFoundException ex) {
            throw new Repo.InstantiationException(ex);
        } catch (InstantiationException ex) {
            throw new Repo.InstantiationException(ex);
        } catch (IllegalAccessException ex) {
            throw new Repo.InstantiationException(ex);
        } catch (InvocationTargetException ex) {
            throw new Repo.InstantiationException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asText() {
        final StringBuilder text = new StringBuilder();
        text.append(this.type).append('(');
        final List<String> kids = new ArrayList<String>(this.vars.length);
        for (Variable<?> var : this.vars) {
            kids.add(var.asText());
        }
        final String line = StringUtils.join(kids, ", ");
        if (line.length() < Tv.FIFTY && !line.contains(Composite.EOL)) {
            text.append(line);
        } else {
            final String shift = new StringBuilder()
                .append(CharUtils.LF).append(Composite.INDENT).toString();
            int idx;
            for (idx = 0; idx < kids.size(); ++idx) {
                if (idx > 0) {
                    text.append(',');
                }
                text.append(shift)
                    .append(kids.get(idx).replace(Composite.EOL, shift));
            }
            if (idx > 0) {
                text.append(CharUtils.LF);
            }
        }
        text.append(')');
        return text.toString();
    }

    /**
     * Find the best matching constructor.
     * @param types Types
     * @return The ctor
     * @throws ClassNotFoundException If class not found
     */
    private Constructor<?> ctor(final Class<?>[] types)
        throws ClassNotFoundException {
        final Class<?> cls = Class.forName(this.type);
        Constructor<?> ctor = null;
        for (Constructor<?> opt : cls.getConstructors()) {
            if (Composite.inherit(opt.getParameterTypes(), types)) {
                ctor = opt;
                break;
            }
        }
        if (ctor == null) {
            throw new IllegalArgumentException(
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
    private static boolean inherit(final Class<?>[] parents,
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
     * @param type Type to auto-box
     * @return Non-scalar type
     */
    private static Class<?> box(final Class<?> type) {
        Class<?> out;
        if (int.class.equals(type)) {
            out = Integer.class;
        } else if (long.class.equals(type)) {
            out = Long.class;
        } else {
            out = type;
        }
        return out;
    }

}
