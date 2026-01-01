/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * GitHub URL for Issue.
 *
 * @since 2.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "url")
final class IssueUrl {

    /**
     * Pattern for issue URL.
     */
    private static final Pattern CORRECT =
        Pattern.compile("https://api.github.com/repos/[^/]+/[^/]+/(?:issues|pulls)/(\\d+).*");

    /**
     * Url.
     */
    private final String url;

    /**
     * Ctor.
     * @param url Issue url.
     */
    IssueUrl(final String url) {
        this.url = url;
    }

    /**
     * Get issue id from url.
     * @return Issue id
     * @checkstyle MethodNameCheck (10 lines)
     */
    public int uid() {
        final Matcher matcher = IssueUrl.CORRECT.matcher(this.url);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalStateException(
            String.format("URL %s is not valid issue url", this.url)
        );
    }

    /**
     * Check if url is a valid url for Issue.
     * @return True if valid
     */
    public boolean valid() {
        if (this.url == null || this.url.isEmpty()) {
            throw new IllegalArgumentException(
                "URL should not be empty"
            );
        }
        return IssueUrl.CORRECT.matcher(this.url).matches();
    }
}
