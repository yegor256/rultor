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
import com.rultor.spi.Repo;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Composite.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
final class Composite implements Variable {

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
    protected Composite(final String name, final Collection<Variable> args) {
        this.type = name;
        this.vars = args.toArray(new Variable[args.size()]);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public Object instantiate() throws Repo.InstantiationException {
        final Object[] args = new Object[this.vars.length];
        final Class<?>[] types = new Class<?>[this.vars.length];
        for (int idx = 0; idx < this.vars.length; ++idx) {
            final Object object = this.vars[idx].instantiate();
            args[idx] = object;
            types[idx] = Composite.typeOf(object);
        }
        try {
            return Class.forName(this.type)
                .getConstructor(types)
                .newInstance(args);
        } catch (ClassNotFoundException ex) {
            throw new Repo.InstantiationException(ex);
        } catch (NoSuchMethodException ex) {
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
        for (int idx = 0; idx < this.vars.length; ++idx) {
            if (idx > 0) {
                text.append(", ");
            }
            text.append(this.vars[idx].asText());
        }
        text.append(')');
        return text.toString();
    }

    /**
     * Get type of object.
     * @param object The object
     * @return Its type
     */
    private static Class<?> typeOf(final Object object) {
        Class<?> cls = object.getClass();
        if (cls.equals(Integer.class)) {
            cls = int.class;
        } else if (cls.equals(Long.class)) {
            cls = long.class;
        } else if (cls.equals(Boolean.class)) {
            cls = boolean.class;
        } else if (cls.equals(Double.class)) {
            cls = double.class;
        }
        return cls;
    }

}
