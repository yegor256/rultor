/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import java.util.Locale;
import lombok.EqualsAndHashCode;

/**
 * Turn talk name into Docker container name.
 *
 * @since 1.72
 */
@Immutable
@EqualsAndHashCode(callSuper = false)
public final class Container {

    /**
     * Talk name.
     */
    private final transient String name;

    /**
     * Ctor.
     * @param talk Script name
     */
    public Container(final String talk) {
        this.name = talk;
    }

    @Override
    public String toString() {
        return this.name.replaceAll("[^a-zA-Z0-9_.-]", "_")
            .toLowerCase(Locale.ENGLISH);
    }

}
