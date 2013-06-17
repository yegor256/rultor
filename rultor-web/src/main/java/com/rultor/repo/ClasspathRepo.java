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

import com.codahale.metrics.MetricRegistry;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Instance;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.User;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

/**
 * Repo on classpath.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
public final class ClasspathRepo implements Repo {

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    @NotNull
    public Instance make(@NotNull final User user, @NotNull final Spec spec)
        throws Repo.InstantiationException {
        Object object;
        try {
            object = this.variable(spec.asText()).instantiate(user);
        } catch (Repo.InvalidSyntaxException ex) {
            throw new Repo.InstantiationException(ex);
        }
        return new RuntimeInstance(object);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    @NotNull
    public Spec make(@NotNull final String text)
        throws Repo.InvalidSyntaxException {
        return this.variable(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final MetricRegistry registry) {
        // nothing to do
    }

    /**
     * Turn text to variable.
     * @param text Text
     * @return Variable
     * @throws Repo.InvalidSyntaxException If fails
     * @checkstyle RedundantThrows (5 lines)
     */
    private Variable variable(final String text)
        throws Repo.InvalidSyntaxException {
        Variable var;
        if (text.startsWith(BigText.PREFIX)) {
            var = new BigText(text.substring(BigText.PREFIX.length()));
        } else {
            var = this.parse(text);
        }
        return var;
    }

    /**
     * Parse text to variable.
     * @param text Text
     * @return Variable
     * @throws Repo.InvalidSyntaxException If fails
     * @checkstyle RedundantThrows (5 lines)
     */
    private Variable parse(final String text)
        throws Repo.InvalidSyntaxException {
        final CharStream input = new ANTLRStringStream(text);
        final SpecLexer lexer = new SpecLexer(input);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final SpecParser parser = new SpecParser(tokens);
        Variable var;
        try {
            var = parser.spec();
        } catch (org.antlr.runtime.RecognitionException ex) {
            throw new Repo.InvalidSyntaxException(ex);
        } catch (IllegalArgumentException ex) {
            throw new InvalidSyntaxException(ex);
        }
        return var;
    }

}
