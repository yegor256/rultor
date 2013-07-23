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
import com.jcabi.immutable.Array;
import com.rultor.spi.Arguments;
import com.rultor.spi.SpecException;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Big text, without any formatting.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "lines")
@Loggable(Loggable.DEBUG)
final class BigText implements Variable<String> {

    /**
     * EOL.
     */
    private static final String EOL = "\n";

    /**
     * Lines of text.
     */
    private final transient Array<String> lines;

    /**
     * Public ctor.
     * @param text Text to parse and encapsulate
     */
    protected BigText(final String text) {
        final String[] items = StringUtils.splitPreserveAllTokens(
            StringUtils.stripStart(
                StringUtils.stripEnd(text, null),
                "\n\r"
            ),
            BigText.EOL
        );
        int min = Integer.MAX_VALUE;
        for (int idx = 0; idx < items.length; ++idx) {
            items[idx] = StringUtils.strip(items[idx], "\r")
                .replaceAll("\\s+$", "")
                .replaceAll("\t", "    ");
            if (items[idx].isEmpty()) {
                continue;
            }
            min = Math.min(
                min,
                items[idx].length()
                - items[idx].replaceAll("^\\s+", "").length()
            );
        }
        for (int idx = 0; idx < items.length; ++idx) {
            if (items[idx].length() > min) {
                items[idx] = items[idx].substring(min);
            } else {
                items[idx] = StringUtils.repeat(' ', min);
            }
        }
        this.lines = new Array<String>(items);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (8 lines)
     */
    @Override
    @NotNull
    public String instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "arguments can't be NULL") final Arguments args)
        throws SpecException {
        return StringUtils.join(this.lines, BigText.EOL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asText() {
        return new StringBuilder("\"\"\"\n")
            .append(StringUtils.join(this.lines, BigText.EOL))
            .append("\n\"\"\"")
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> arguments() {
        return new ConcurrentHashMap<Integer, String>(0);
    }

}
