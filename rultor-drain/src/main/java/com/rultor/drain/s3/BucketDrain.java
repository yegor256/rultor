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
package com.rultor.drain.s3;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.aws.S3Client;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;

/**
 * Drain shared among multiple S3 objects.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "work", "client" })
@Loggable(Loggable.DEBUG)
public final class BucketDrain implements Drain {

    /**
     * The work it is busy with at the moment.
     */
    private final transient Work work;

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * Public ctor.
     * @param clnt S3 client
     * @param wrk Work we're in now
     */
    public BucketDrain(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "S3 client can't be NULL") final S3Client clnt) {
        this.client = clnt;
        this.work = wrk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "S3 objects in %s",
            this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() {
        return new BucketPulses(this.client, this.prefix(), new Time());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        this.obj().append(lines);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "BucketDrain: work='%s', client='%s'\n",
                    this.work,
                    this.client
                )
            ),
            this.obj().read()
        );
    }

    /**
     * Make an object drain.
     * @return Object drain
     */
    private ObjectDrain obj() {
        return new ObjectDrain(
            this.client,
            String.format(
                "%s%s",
                this.prefix(),
                new Key(this.work.scheduled()).toString()
            )
        );
    }

    /**
     * Make a prefix.
     * @return Prefix to use
     */
    private String prefix() {
        return String.format(
            "%s/%s/",
            this.work.owner(),
            this.work.rule()
        );
    }

}
