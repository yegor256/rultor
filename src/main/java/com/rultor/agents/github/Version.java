/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates a release's version number.
 *
 * @author Jimmy Spivey (JimDeanSpivey@gmail.com)
 * @version $Id$
 * @since 1.57
 */
@Immutable
public final class Version {

    /**
     * Version pattern.
     */
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("\\.?(?:\\d+\\.)*\\d+");

    /**
     * The proposed version tag.
     */
    private final transient String tag;

    /**
     * Constructor with just the tag field.
     * @param ver The version number of a release.
     */
    public Version(final String ver) {
        this.tag = ver;
    }

    /**
     * Is the version number format valid. For example:
     * Valid version numbers:
     * .1
     * 2.2
     * .1.2
     * 1.2.3.4.5.6.7
     *
     * Invalid version numbers:
     * abc
     * a.b.c
     * 1.
     * 1.2.
     * @return True if the tag is valid, false otherwise
     */
    public boolean isValid() {
        final Matcher matcher = VERSION_PATTERN.matcher(this.tag);
        return matcher.matches();
    }

    /**
     * Returns the proposed tag originally passed in the constructor.
     * @return The proposed tag
     */
    @Override
    public String toString() {
        return this.tag;
    }
}
