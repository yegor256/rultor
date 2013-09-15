/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.scm.git;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wrapper for string representation of GIT URI.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "uri")
@Loggable(Loggable.DEBUG)
public final class GitURI {

    /**
     * Pattern to validate GIT URLS.
     */
    private static final Pattern PATTERN =
        Pattern.compile(
            // @checkstyle StringLiteralsConcatenation (6 lines)
            "ssh://(\\w+@)?\\w+[\\w.-]*(:\\d+)?/\\w[\\w./-]+\\w.git/?|"
            + "(git|((http|ftp)s?))://\\w+[\\w.-]*(:\\d+)?/\\w[\\w./-]+\\w"
            + ".git/?|rsync://\\w+[\\w.-/]*.git/?|"
            + "(\\w+@)?[\\w.-]+:(?!(/))[\\w.-/]+|"
            + "/\\w+[\\w/]+\\w+.git/?|file:///[\\w/]+\\w+.git/?"
        );

    /**
     * Underlying uri value.
     */
    private final transient String uri;

    /**
     * Public ctor.
     * Throws IllegalArgumentException if passed address is invalid
     * @param addr GIT URL
     */
    public GitURI(final String addr) {
        if (!GitURI.PATTERN.matcher(addr).matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid GIT URL: %s", addr)
            );
        }
        this.uri = addr;
    }

    /**
     * Getting underlying value.
     * @return Underlying uri value
     */
    public String getValue() {
        return this.uri;
    }
}
