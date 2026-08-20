/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import java.util.Collection;
import java.util.Locale;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;

/**
 * Repositories that are allowed to use the assets of another repository.
 *
 * <p>They are listed in the {@code friends} section of {@code .rultor.yml},
 * in the repository that owns the assets. Names are compared without
 * case sensitivity. A name may end with an asterisk, which permits every
 * repository of a GitHub account, as in {@code jcabi/*}.</p>
 *
 * @since 2.1
 */
final class Friends {

    /**
     * Names, in lower case.
     */
    private final Collection<String> names;

    /**
     * Ctor.
     * @param items Names, as they are listed in .rultor.yml
     */
    Friends(final Iterable<String> items) {
        this.names = new ListOf<>(
            new Mapped<>(item -> item.toLowerCase(Locale.ENGLISH), items)
        );
    }

    /**
     * How many of them are there?
     * @return Total count of names in the list
     */
    int size() {
        return this.names.size();
    }

    /**
     * Is this repository among them?
     * @param coords Coordinates of the repo, e.g. "yegor256/rultor"
     * @return TRUE if the repo may use the assets
     */
    boolean allow(final String coords) {
        final String repo = coords.toLowerCase(Locale.ENGLISH);
        boolean allowed = false;
        for (final String name : this.names) {
            if (name.equals(repo) || Friends.matches(name, repo)) {
                allowed = true;
                break;
            }
        }
        return allowed;
    }

    private static boolean matches(final String name, final String repo) {
        return name.endsWith("/*")
            && repo.startsWith(name.substring(0, name.length() - 1));
    }
}
