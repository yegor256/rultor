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
package com.rultor.base;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.LogExceptions;
import com.jcabi.aspects.Loggable;
import com.jcabi.manifests.Manifests;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Instance;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import lombok.EqualsAndHashCode;
import org.xembly.Directives;

/**
 * Descriptive instance that tells about itself in the xembly log.
 *
 * <p>It is important to use this instance wrapper for
 * <strong>all instances</strong>. It will enable their visibility in
 * the management panel and in the stand. Simply wrap whatever instance you
 * have in the unit into this wrapper:
 *
 * <pre> com.rultor.base.Descriptive(
 *   ${0:?}, my-instance-to-wrap
 * )</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "work", "origin" })
@Loggable(Loggable.DEBUG)
public final class Descriptive implements Instance {

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Origin.
     */
    private final transient Instance origin;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param instance Original instance
     */
    public Descriptive(final Work wrk, final Instance instance) {
        this.work = wrk;
        this.origin = instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @LogExceptions
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        final long start = System.currentTimeMillis();
        new XemblyLine(
            new Directives()
                .xpath("/snapshot[not(work)]")
                .strict(1)
                .add("work")
                .add("owner")
                .set(this.work.owner().toString())
                .up()
                .add("rule")
                .set(this.work.rule())
                .up()
                .add("scheduled")
                .set(this.work.scheduled().toString())
        ).log();
        new XemblyLine(
            new Directives()
                .xpath("/snapshot[not(start)]")
                .strict(1)
                .add("start")
                .set(new Time().toString())
        ).log();
        new XemblyLine(
            new Directives()
                .xpath("/snapshot[not(stdout)]")
                .strict(1)
                .add("stdout")
                .set(this.work.stdout().toString())
        ).log();
        new XemblyLine(
            new Directives()
                .xpath("/snapshot[not(version)]")
                .strict(1)
                .add("version")
                .add("name")
                .set(Manifests.read("Rultor-Version"))
                .up()
                .add("revision")
                .set(Manifests.read("Rultor-Revision"))
                .up()
                .add("date")
                .set(Manifests.read("Rultor-Date"))
        ).log();
        try {
            this.origin.pulse();
        } finally {
            new XemblyLine(
                new Directives().xpath("/snapshot/stdout").strict(1).remove()
            ).log();
            new XemblyLine(
                new Directives()
                    .xpath("/snapshot[not(finish)]")
                    .strict(1)
                    .add("finish")
                    .set(new Time().toString())
            ).log();
            new XemblyLine(
                new Directives()
                    .xpath("/snapshot[not(duration)]")
                    .strict(1)
                    .add("duration")
                    .set(Long.toString(System.currentTimeMillis() - start))
            ).log();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("verbose %s", this.origin);
    }

}
