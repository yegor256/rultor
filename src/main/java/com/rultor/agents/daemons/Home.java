/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Home page of a daemon.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "xml", "hash" })
public final class Home {

    /**
     * Talk.
     */
    private final transient XML xml;

    /**
     * Hash.
     */
    private final transient String hash;

    /**
     * Ctor.
     * @param talk Talk
     */
    public Home(final XML talk) {
        this(talk, talk.xpath("/talk/request/@id").get(0));
    }

    /**
     * Ctor.
     * @param talk Talk
     * @param hsh Hash
     */
    public Home(final XML talk, final String hsh) {
        this.xml = talk;
        this.hash = hsh;
    }

    /**
     * Get its URI.
     * @return URI
     */
    public URI uri() {
        return URI.create(
            String.format(
                "https://www.rultor.com/t/%d-%s",
                Long.parseLong(this.xml.xpath("/talk/@number").get(0)),
                this.hash
            )
        );
    }

}
