/**
 * Copyright (c) 2009-2018, rultor.com
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
package com.rultor.cached;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.iterable.Mapped;

/**
 * Cached talks.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.51
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
@SuppressWarnings("PMD.TooManyMethods")
public final class CdTalks implements Talks {

    /**
     * Original talks.
     */
    private final transient Talks origin;

    /**
     * Public ctor.
     * @param talks Talks
     */
    public CdTalks(final Talks talks) {
        this.origin = talks;
    }

    @Override
    @Cacheable
    public boolean exists(final long number) {
        return this.origin.exists(number);
    }

    @Override
    @Cacheable
    public Talk get(final long number) {
        return this.origin.get(number);
    }

    @Override
    @Cacheable
    public boolean exists(final String name) {
        return this.origin.exists(name);
    }

    @Override
    @Cacheable
    public Talk get(final String name) {
        return this.origin.get(name);
    }

    @Override
    @Cacheable.FlushBefore
    public void delete(final String name) {
        this.origin.delete(name);
    }

    @Override
    @Cacheable.FlushBefore
    public void create(final String repo, final String name)
        throws IOException {
        this.origin.create(repo, name);
    }

    @Override
    @Cacheable
    public Iterable<Talk> active() {
        return new Mapped<>(
            input -> new CdTalk(input),
            this.origin.active()
        );
    }

    @Override
    @Cacheable(lifetime = Tv.TWENTY, unit = TimeUnit.MINUTES)
    public Iterable<Talk> recent() {
        return new Mapped<>(
            input -> new CdTalk(input),
            this.origin.recent()
        );
    }

    @Override
    @Cacheable
    public Iterable<Talk> siblings(final String repo, final Date since) {
        return new Mapped<>(
            input -> new CdTalk(input),
            this.origin.siblings(repo, since)
        );
    }
}
