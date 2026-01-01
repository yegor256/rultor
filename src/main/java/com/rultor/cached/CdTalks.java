/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.cached;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
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
 * @since 1.51
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
            CdTalk::new,
            this.origin.active()
        );
    }

    @Override
    @Cacheable(lifetime = 20, unit = TimeUnit.MINUTES)
    public Iterable<Talk> recent() {
        return new Mapped<>(
            CdTalk::new,
            this.origin.recent()
        );
    }

    @Override
    @Cacheable
    public Iterable<Talk> siblings(final String repo, final Date since) {
        return new Mapped<>(
            CdTalk::new,
            this.origin.siblings(repo, since)
        );
    }
}
