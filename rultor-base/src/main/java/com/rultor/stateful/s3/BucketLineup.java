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
package com.rultor.stateful.s3;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.aws.S3Client;
import com.rultor.spi.Work;
import com.rultor.stateful.Lineup;
import java.util.concurrent.Callable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Lineups in S3 bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "work", "client", "prefix" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class BucketLineup implements Lineup {

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * S3 object prefix name.
     */
    private final transient String prefix;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param pfx Prefix
     * @param clnt Client
     */
    public BucketLineup(@NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "S3 prefix can't be NULL") final String pfx,
        @NotNull(message = "S3 client can't be NULL") final S3Client clnt) {
        this.work = wrk;
        this.prefix = pfx;
        this.client = clnt;
    }

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param clnt Client
     */
    public BucketLineup(final Work wrk, final S3Client clnt) {
        this(wrk, "", clnt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "S3 lineups in `%s` with `%s` prefix accessed with %s",
            this.client.bucket(), this.prefix, this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T exec(final Callable<T> callable) throws Exception {
        return this.lineup().exec(callable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void exec(final Runnable runnable) {
        this.lineup().exec(runnable);
    }

    /**
     * Make an underlying lineup.
     * @return The lineup
     */
    private Lineup lineup() {
        return new ObjectLineup(
            String.format(
                "%s%s/%s.lineup",
                this.prefix,
                this.work.owner(),
                this.work.rule()
            ),
            this.client
        );
    }

}
