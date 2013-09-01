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
package com.rultor.web;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rexsl.page.JaxbBundle;
import com.rultor.spi.Arguments;
import com.rultor.spi.Drain;
import com.rultor.spi.Repo;
import com.rultor.spi.Rule;
import com.rultor.spi.Spec;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Markdown;
import com.rultor.tools.Time;
import java.net.URI;
import lombok.EqualsAndHashCode;

/**
 * Face of {@link Spec} in JAXB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "repo", "users" })
final class JaxbFace {

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Users.
     */
    private final transient Users users;

    /**
     * Public ctor.
     * @param rpo Repo
     * @param usrs Users
     */
    protected JaxbFace(final Repo rpo, final Users usrs) {
        this.repo = rpo;
        this.users = usrs;
    }

    /**
     * Build bundle.
     * @param user URN of the user
     * @param rule Rule of the user
     * @return Bundle
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public JaxbBundle bundle(final User user, final Rule rule) {
        final Spec spec = rule.spec();
        JaxbBundle bundle = new JaxbBundle("face");
        try {
            final Variable<?> var = new Repo.Cached(
                this.repo, user, spec
            ).get();
            if (var.arguments().isEmpty()) {
                final Object object = var.instantiate(
                    this.users,
                    new Arguments(
                        this.work(user.urn(), rule.name()),
                        new Wallet.Empty()
                    )
                );
                bundle = this.append(bundle, object);
            } else {
                bundle = bundle.add("arguments").add(
                    new JaxbBundle.Group<String>(var.arguments().values()) {
                        @Override
                        public JaxbBundle bundle(final String arg) {
                            return new JaxbBundle("argument", arg);
                        }
                    }
                ).up();
            }
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            bundle = bundle.add(
                "exception", Exceptions.stacktrace(ex)
            ).up();
        }
        return bundle;
    }

    /**
     * Append object elements to the bundle.
     * @param bundle Bundle to append to
     * @param object Object to describe
     * @return Bundle
     */
    private JaxbBundle append(final JaxbBundle bundle, final Object object) {
        JaxbBundle output = bundle
            .add("type", object.getClass().getName())
            .up()
            .add(
                "drainable",
                Boolean.toString(object instanceof Drain.Source)
            )
            .up();
        if (!(object instanceof String)) {
            output = output.add(
                "html", new Markdown(object.toString()).html()
            ).up();
        }
        return output;
    }

    /**
     * The work we're in (while rendering).
     * @param owner Owner of the rule
     * @param rule Name of the rule we're rendering now
     * @return The work
     */
    private Work work(final URN owner, final String rule) {
        // @checkstyle AnonInnerLength (50 lines)
        return new Work() {
            @Override
            public Time scheduled() {
                return new Time();
            }
            @Override
            public URN owner() {
                return owner;
            }
            @Override
            public String rule() {
                return rule;
            }
            @Override
            public URI stdout() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
