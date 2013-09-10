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
package com.rultor.conveyer;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.log4j.ThreadGroupSpy;
import com.rultor.spi.Arguments;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Drain;
import com.rultor.spi.Instance;
import com.rultor.spi.Repo;
import com.rultor.spi.Rule;
import com.rultor.spi.Spec;
import com.rultor.spi.SpecException;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * One job to do.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "work", "repo", "users" })
@SuppressWarnings("PMD.DoNotUseThreads")
final class Job {

    /**
     * Decorator of an instance.
     */
    public interface Decor {
        /**
         * Decorate this given instance.
         * @param instance Instance to decorate
         * @return Decorated one
         */
        Instance decorate(Instance instance);
    }

    /**
     * Work to do.
     */
    private final transient Coordinates work;

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
     * @param wrk Work to do
     * @param rep Repo
     * @param usrs Users
     */
    protected Job(final Coordinates wrk, final Repo rep, final Users usrs) {
        this.work = wrk;
        this.repo = rep;
        this.users = usrs;
    }

    /**
     * Process given work.
     * @param decor Decorator to use
     * @throws Exception If fails
     */
    public void process(final Job.Decor decor) throws Exception {
        final User owner = this.users.get(this.work.owner());
        final Rule rule = owner.rules().get(this.work.rule());
        final Variable<?> var = this.var(owner, rule.spec());
        if (var.arguments().isEmpty()) {
            final Arguments args = new Arguments(
                this.work, new OwnWallet(this.work, rule)
            );
            final Object instance = var.instantiate(this.users, args);
            final Object drain = this.var(owner, rule.drain())
                .instantiate(this.users, args);
            if (instance instanceof Instance) {
                new ThreadGroupSpy(
                    this.work,
                    new WithSpec(
                        rule.spec(),
                        decor.decorate(Instance.class.cast(instance))
                    ),
                    Drain.class.cast(drain)
                ).pulse();
            }
        }
    }

    /**
     * Make a variable.
     * @param owner Owner of the spec
     * @param spec Spec itself
     * @throws SpecException If fails
     */
    private Variable<?> var(final User owner, final Spec spec)
        throws SpecException {
        return new Repo.Cached(this.repo, owner, spec).get();
    }

}
