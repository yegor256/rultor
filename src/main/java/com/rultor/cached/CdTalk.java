/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.cached;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;

/**
 * Cached talk.
 *
 * @since 1.51
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class CdTalk implements Talk {
    /**
     * Origin talk.
     */
    private final transient Talk origin;

    /**
     * Ctor.
     * @param talk Talks
     */
    CdTalk(final Talk talk) {
        this.origin = talk;
    }

    @Override
    @Cacheable
    public Long number() throws IOException {
        return this.origin.number();
    }

    @Override
    @Cacheable
    public String name() throws IOException {
        return this.origin.name();
    }

    @Override
    @Cacheable
    public Date updated() throws IOException {
        return this.origin.updated();
    }

    @Override
    @Cacheable
    public XML read() throws IOException {
        return this.origin.read();
    }

    @Override
    @Cacheable.FlushBefore
    public void modify(final Iterable<Directive> dirs) throws IOException {
        this.origin.modify(dirs);
    }

    @Override
    @Cacheable.FlushBefore
    public void active(final boolean yes) throws IOException {
        this.origin.active(yes);
    }

}
