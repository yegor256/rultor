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
package com.rultor.repo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Big text, without any formatting.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "value")
@Loggable(Loggable.DEBUG)
final class BigText implements Variable<String> {

    /**
     * Pattern to match.
     */
    private static final Pattern PTN = Pattern.compile(
        "java.lang.String:\\s*\n(.*)", Pattern.MULTILINE | Pattern.DOTALL
    );

    /**
     * The value.
     */
    private final transient String value;

    /**
     * Public ctor.
     * @param text Text to parse and encapsulate
     */
    protected BigText(final String text) {
        final Matcher matcher = BigText.PTN.matcher(text);
        Validate.isTrue(matcher.matches(), "invalid input '%s'", text);
        this.value = matcher.group(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String instantiate(final Users users) {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asText() {
        return new StringBuilder("java.lang.String:\n")
            .append(this.value).toString();
    }

    /**
     * Text looks like big text?
     * @param text The text
     * @return TRUE if it looks like one
     */
    public static boolean matches(final String text) {
        return BigText.PTN.matcher(text).matches();
    }

}
