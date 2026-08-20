/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.dynamo;

import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for Talks.
 * @since 1.1
 */
final class TalkMatcher extends TypeSafeMatcher<Talk> {

    /**
     * Name of the talk.
     */
    private final transient String name;

    /**
     * Constructor.
     * @param nam Name of the talk
     */
    TalkMatcher(final String nam) {
        super();
        this.name = nam;
    }

    @Override
    public boolean matchesSafely(final Talk talk) {
        try {
            return talk.name().equals(this.name);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(
            String.format("Talk '%s' not found", this.name)
        );
    }
}
