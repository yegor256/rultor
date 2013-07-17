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
import com.rultor.spi.Arguments;
import com.rultor.spi.Proxy;
import com.rultor.spi.SpecException;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
     * Supplementary method name.
     */
    public static final String METHOD = "__rultor_setToString";

    /**
     * Supplementary field name.
     */
    private static final String FIELD = "__rultor_toString";

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
     * @checkstyle RedundantThrows (10 lines)
     */
    @Override
    @NotNull
    public Object instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "arguments can't be NULL") final Arguments arguments)
        throws SpecException {
        final Object[] args = new Object[this.vars.length];
        final Class<?>[] types = new Class<?>[this.vars.length];
        for (int idx = 0; idx < this.vars.length; ++idx) {
            final Object object = this.vars[idx].instantiate(users, arguments);
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
                    ExceptionUtils.getRootCauseMessage(ex)
                ),
                ex
            );
        } catch (IllegalAccessException ex) {
            throw new SpecException(
                String.format(
                    "failed to access \"%s\": %s",
                    ctor,
                    ExceptionUtils.getRootCauseMessage(ex)
                ),
                ex
            );
        } catch (InvocationTargetException ex) {
            throw new SpecException(
                String.format(
                    "failed to invoke \"%s\": %s",
                    ctor,
                    ExceptionUtils.getRootCauseMessage(ex)
                ),
                ex
            );
        }
        if (object instanceof Proxy) {
            object = Proxy.class.cast(object).object();
        }
        return object;
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
     * @throws SpecException If can't get it
     * @checkstyle RedundantThrows (5 lines)
     */
    private Constructor<?> ctor(final Class<?>[] types)
        throws SpecException {
        final Class<?> cls;
        if (this.type.startsWith("java.")) {
            try {
                cls = Class.forName(this.type);
            } catch (ClassNotFoundException ex) {
                throw new SpecException(ex);
            }
        } else {
            cls = Composite.load(this.type);
        }
        Constructor<?> ctor = null;
        for (Constructor<?> opt : cls.getConstructors()) {
            if (Composite.inherit(opt.getParameterTypes(), types)) {
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
        } else if (double.class.equals(type)) {
            out = Double.class;
        } else if (boolean.class.equals(type)) {
            out = Boolean.class;
        } else {
            out = type;
        }
        return out;
    }

    /**
     * Load and alter class.
     * @param name Class name
     * @return Modified type
     * @throws SpecException If can't instantiate
     * @checkstyle RedundantThrows (4 lines)
     */
    private static Class<?> load(final String name)
        throws SpecException {
        final ClassPool pool = ClassPool.getDefault();
        Class<?> type;
        try {
            final CtClass cls = pool.get(name);
            if (Composite.loaded(name) || cls.isModified()) {
                type = Class.forName(name);
            } else {
                final CtField field = new CtField(
                    pool.get("java.lang.String"), Composite.FIELD, cls
                );
                field.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
                cls.addField(field);
                final String body = String.format(
                    "if (%s != null) return %<s;",
                    Composite.FIELD
                );
                if (Composite.hasToString(cls)) {
                    final CtMethod method =
                        // @checkstyle MultipleStringLiterals (1 line)
                        cls.getDeclaredMethod("toString", new CtClass[0]);
                    method.insertBefore(body);
                } else {
                    cls.addMethod(
                        CtMethod.make(
                            String.format(
                                // @checkstyle StringLiteralsConcatenation (5 lines)
                                "public String toString() { "
                                + "%s return super.toString(); }",
                                body
                            ),
                            cls
                        )
                    );
                }
                cls.addMethod(
                    CtMethod.make(
                        String.format(
                            // @checkstyle StringLiteralsConcatenation (5 lines)
                            "public void %s(String value) {"
                            + "java.lang.reflect.Field field = "
                            + "  this.getClass().getDeclaredField(\"%s\");"
                            + "field.setAccessible(true);"
                            + "field.set(this, value);}",
                            Composite.METHOD,
                            Composite.FIELD
                        ),
                        cls
                    )
                );
                type = cls.toClass();
                cls.defrost();
            }
        } catch (NotFoundException ex) {
            throw new SpecException(
                String.format(
                    "not found \"%s\"",
                    name
                ),
                ex
            );
        } catch (CannotCompileException ex) {
            throw new SpecException(
                String.format(
                    "can't compile \"%s\": %s",
                    name,
                    ExceptionUtils.getRootCauseMessage(ex)
                ),
                ex
            );
        } catch (ClassNotFoundException ex) {
            throw new SpecException(
                String.format(
                    "class \"%s\" not found: %s",
                    name,
                    ExceptionUtils.getRootCauseMessage(ex)
                ), ex
            );
        }
        return type;
    }

    /**
     * Does it have toString() method already?
     * @param type The type
     * @return TRUE if it has it
     * @throws NotFoundException If not found
     * @checkstyle RedundantThrows (5 lines)
     */
    private static boolean hasToString(final CtClass type)
        throws NotFoundException {
        boolean has = false;
        for (CtMethod method : type.getDeclaredMethods()) {
            if ("toString".equals(method.getName())
                && method.getParameterTypes().length == 0) {
                has = true;
                break;
            }
        }
        return has;
    }

    /**
     * Is it loaded already?
     * @param name Class name
     * @return TRUE if it's already loaded
     * @throws SpecException If can't instantiate
     * @checkstyle RedundantThrows (4 lines)
     */
    private static boolean loaded(final String name)
        throws SpecException {
        try {
            final Method method = ClassLoader.class.getDeclaredMethod(
                "findLoadedClass", String.class
            );
            method.setAccessible(true);
            return method.invoke(
                ClassLoader.getSystemClassLoader(), name
            ) != null;
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        } catch (SecurityException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
