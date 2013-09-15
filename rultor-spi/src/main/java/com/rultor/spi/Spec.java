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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Specification of an object (aka "Spec").
 *
 * <p>A spec is a plain-text description of a Java object
 * to be instantiated. This is a simple example of a spec:
 *
 * <pre> com.rultor.base.Base64("AFEFEFSJKL789jojHKLW==")</pre>
 *
 * <p>Formally, a spec is defined as (simplified BNF):
 *
 * <pre> spec := variable;
 * variable := composite
 *   | name arguments | urn ':' name arguments
 *   | array | dictionary | meta | arg
 *   | text | bigtext
 *   | integer | double | long | boolean;
 * arguments := '(' ( variable ( ',' variable )? )? ')';
 * name := [a-z0-9-]+;
 * urn := 'urn:[a-z]+:[0-9]+';
 * composite := type arguments;
 * type := [a-z0-9$-]+;
 * array := '[' ( variable ( ',' variable )? )? ']';
 * dictionary := '{' ( text ':' variable ( ',' text ':' variable )? )? '}';
 * meta := '$' '{' [a-z] '}';
 * arg := '$' '{' [0-9] ':' .* '}';
 * text := '"' ('\\"' | ~'"')* '"';
 * bigtext := '"""' .+ '"""';
 * integer := (+|-)? [0-9]+;
 * double := (+|-)? [0-9]+ '.' [0-9]+;
 * long := (+|-)? [0-9]+ 'L';
 * boolean := 'TRUE' | 'FALSE';</pre>
 *
 * <p>This description actually means that a spec is a variable, and there are
 * a few possible kinds of variables. The most commonly used is a "composite",
 * which represents a Java class to be instantiated. For example:
 *
 * <pre> com.rultor.base.Empty()</pre>
 *
 * Another one is a local reference to a rule, for example:
 *
 * <pre> my-other-rule()</pre>
 *
 * <p>This spec actually tells its parser that the object should be
 * instantiated using the spec from another rule, named
 * {@code my-other-rule}. It's also possible to refer to the rule
 * of another user:
 *
 * <pre> urn:github:526301:his-rule()</pre>
 *
 * <p>This spec tells its parser to go to the rule {@code his-rule}
 * that belongs to the user {@code urn:github:526301}.
 *
 * <p>Arrays and dictionaries are collections and maps in Java, for example:
 *
 * <pre> [ "first line", "second line" ]
 * {
 *   "name": "Jeff Lebowski",
 *   "photo": java.net.URI("http://.."),
 *   "age": 32
 * }
 * [ com.rultor.base.Empty(), 4, 67L, 14.989008 ]</pre>
 *
 * <p>As you see, both arrays and dictionaries are not strongly typed and
 * may contains objects of different types.
 *
 * <p>Texts have two forms, a short/compact/usual one, and a long one,
 * called a "big text". Both of them create instances of Java
 * {@link java.lang.String} class, but big text is more convenient for
 * multi-line blocks of text, for example:
 *
 * <pre> "Hello, \"World!\""
 * """
 * &lt;envelope&gt;
 *   &lt;message&gt;Hello, "World!"&lt;/message&gt;
 * &lt;/envelope&gt;
 * """
 * </pre>
 *
 * <p>So called "metas" may give you access to a few system properties
 * of a running rule, including, for example, {@code ${work}} and
 * {@code ${wallet}}. Some classes need these types of arguments in order
 * to understand where they are running (what are the coordinates) or who
 * to charge for resource usage, for example:
 *
 * <pre> com.rultor.base.Crontab(
 *   ${work}, "*5 * * * *",
 *   com.rultor.base.Empty()
 * )</pre>
 *
 * <p>When you're writing a template, which will be used by other
 * rules or even by other users, you will need to make it parametrized. For
 * example, you want to define a spec template that will send emails, but
 * an actual delivery address should be configurable by those who is
 * using the template:
 *
 * <pre> com.example.Send("Hello, it works!", ${0:email address})</pre>
 *
 * <p>In this example, the spec will instantiate class {@code com.example.Send}
 * with two parameters. The first one is a Java {@code java.lang.String},
 * while the second one is not defined yet, but has to be provided by
 * those who is using this rule. If the name of the rule is, say, {@code send},
 * than it can be used as:
 *
 * <pre> send("me&#64;example.com")</pre>
 *
 * <p>Most of this magic is implemented in {@code rultor-repo} module.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Spec {

    /**
     * Convert it to a human readable form.
     * @return The text
     */
    @NotNull(message = "spec text is never NULL")
    String asText();

    /**
     * Simple.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "text")
    @Loggable(Loggable.DEBUG)
    final class Simple implements Spec {
        /**
         * The text.
         */
        private final transient String text;
        /**
         * Public ctor.
         */
        public Simple() {
            this("com.rultor.base.Empty()");
        }
        /**
         * Public ctor.
         * @param spec The text
         */
        public Simple(@NotNull(message = "spec can't be NULL")
            final String spec) {
            this.text = spec;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String asText() {
            return this.text;
        }
    }

    /**
     * Strict spec.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "spec")
    @Loggable(Loggable.DEBUG)
    final class Strict implements Spec {
        /**
         * Simple spec that passed all quality controls.
         */
        private final transient Spec spec;
        /**
         * Public ctor.
         * @param text The text
         * @param repo Repo
         * @param user User
         * @param users Users
         * @param work Coordinates we're in
         * @param type Type expected
         * @throws SpecException If fails
         * @checkstyle ParameterNumber (10 lines)
         */
        public Strict(
            @NotNull(message = "spec can't be NULL") final String text,
            @NotNull(message = "repo can't be NULL") final Repo repo,
            @NotNull(message = "user can't be NULL") final User user,
            @NotNull(message = "users can't be NULL") final Users users,
            @NotNull(message = "work can't be NULL") final Coordinates work,
            @NotNull(message = "type can't be NULL") final Class<?> type)
            throws SpecException {
            final Spec temp = new Spec.Simple(text);
            final Variable<?> var = repo.make(user, temp);
            if (var.arguments().isEmpty()) {
                final Object object = var.instantiate(
                    users, new Arguments(work, new Wallet.Empty())
                );
                try {
                    object.toString();
                } catch (SecurityException ex) {
                    throw new SpecException(ex);
                }
                if (!type.isAssignableFrom(object.getClass())) {
                    throw new SpecException(
                        String.format(
                            "%s expected while %s provided",
                            type.getName(),
                            object.getClass().getName()
                        )
                    );
                }
            }
            this.spec = new Spec.Simple(var.asText());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String asText() {
            return this.spec.asText();
        }
    }

}
