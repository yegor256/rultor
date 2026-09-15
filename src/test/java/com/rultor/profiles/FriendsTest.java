/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link Friends}.
 *
 * @since 2.1
 */
final class FriendsTest {

    @Test
    void allowsExactName() {
        MatcherAssert.assertThat(
            "Friend listed by its full name cannot be rejected",
            new Friends(new ListOf<>("jeff/hello", "donald/bye"))
                .allow("donald/bye"),
            Matchers.is(true)
        );
    }

    @Test
    void allowsNameInDifferentCase() {
        MatcherAssert.assertThat(
            "Friend spelled in another case cannot be rejected",
            new Friends(new ListOf<>("RoRoche/home")).allow("roroche/HOME"),
            Matchers.is(true)
        );
    }

    @Test
    void allowsEntireAccount() {
        MatcherAssert.assertThat(
            "Asterisk cannot stop covering all repos of the account",
            new Friends(new ListOf<>("jcabi/*")).allow("jcabi/aspects"),
            Matchers.is(true)
        );
    }

    @Test
    void deniesAccountWithSimilarPrefix() {
        MatcherAssert.assertThat(
            "Asterisk cannot leak into a similarly named account",
            new Friends(new ListOf<>("jcabi/*")).allow("jcabi-more/aspects"),
            Matchers.is(false)
        );
    }

    @Test
    void deniesStranger() {
        MatcherAssert.assertThat(
            "Repo out of the list cannot be allowed",
            new Friends(new ListOf<>("jeff/hello", "jcabi/*"))
                .allow("donald/bye"),
            Matchers.is(false)
        );
    }

    @Test
    void countsAllNames() {
        MatcherAssert.assertThat(
            "Every name in the list cannot go uncounted",
            new Friends(new ListOf<>("jeff/hello", "jcabi/*")).size(),
            Matchers.equalTo(2)
        );
    }
}
