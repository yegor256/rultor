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
import com.rultor.snapshot.XemblyDetail;
import com.rultor.spi.Instance;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.xembly.XemblyBuilder;

/**
 * Verbose instance.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "work", "origin" })
@Loggable(Loggable.DEBUG)
public final class Verbose implements Instance {

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
    public Verbose(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "instance can't be NULL") final Instance instance) {
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
        XemblyDetail.log(
            new XemblyBuilder()
                .xpath("/snapshot[not(work)]")
                .add("work")
                .add("owner")
                .set(this.work.owner().toString())
                .up()
                .add("unit")
                .set(this.work.unit())
                .up()
                .add("started")
                .set(this.work.started().toString())
        );
        XemblyDetail.log(
            new XemblyBuilder()
                .xpath("/snapshot[not(started)]")
                .add("start")
                .set(new Time().toString())
        );
        XemblyDetail.log(
            new XemblyBuilder()
                .xpath("/snapshot[not(version)]")
                .add("version")
                .set(
                    String.format(
                        "%s %s %s",
                        Manifests.read("Rultor-Version"),
                        Manifests.read("Rultor-Revision"),
                        Manifests.read("Rultor-Date")
                    )
                )
        );
        XemblyDetail.log(
            new XemblyBuilder()
                .xpath("/snapshot[not(spec)]")
                .add("spec")
                .set(this.work.spec().asText())
        );
        this.origin.pulse();
        XemblyDetail.log(
            new XemblyBuilder()
                .xpath("/snapshot[not(finished)]")
                .add("finished")
                .set(new Time().toString())
        );
        XemblyDetail.log(
            new XemblyBuilder()
                .xpath("/snapshot[not(duration)]")
                .add("duration")
                .set(Long.toString(System.currentTimeMillis() - start))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("verbose %s", this.origin);
    }

}
