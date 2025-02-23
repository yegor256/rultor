/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import com.jcabi.ssh.Ssh;
import org.cactoos.iterable.Mapped;

/**
 * List of texts for the script, in brackets.
 *
 * @since 1.64
 */
final class Brackets {

    /**
     * Items.
     */
    private final transient Iterable<String> items;

    /**
     * Ctor.
     * @param list List of them
     */
    Brackets(final Iterable<String> list) {
        this.items = list;
    }

    @Override
    public String toString() {
        return String.format(
            "( %s )",
            String.join(
                " ",
                new Mapped<>(
                    Ssh::escape,
                    this.items
                )
            )
        );
    }

}
