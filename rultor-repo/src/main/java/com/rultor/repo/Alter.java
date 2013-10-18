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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Arguments;
import com.rultor.spi.SpecException;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.tools.Vext;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Alter.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "value")
@Loggable(Loggable.DEBUG)
final class Alter implements Variable<String>, Comparable<Variable<String>> {

    /**
     * Pattern to match macros.
     */
    private static final Pattern MACROS = Pattern.compile(
        "#arg\\(\\s*(\\d+)\\s*,\\s*'([^']*)'\\s*\\)"
    );

    /**
     * The value.
     */
    private final transient String value;

    /**
     * Public ctor.
     * @param val Value
     */
    protected Alter(final String val) {
        this.value = val;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (10 lines)
     */
    @Override
    @NotNull(message = "text produced is never NULL")
    public String instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "arguments can't be NULL") final Arguments args)
        throws SpecException {
        return new Vext(
            new StringBuilder()
                .append("#macro(arg $pos $desc)$arguments.get($pos)#end#**#")
                .append(this.value)
                .toString()
        ).print(
            new ImmutableMap.Builder<String, Object>()
                .put("work", args.work())
                .put("wallet", args.wallet())
                .put("arguments", args)
                .build()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "text is never NULL")
    public String asText() {
        return String.format(
            "@(\"%s\")",
            StringEscapeUtils.escapeJava(this.value)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> arguments() {
        final ImmutableMap.Builder<Integer, String> args =
            new ImmutableMap.Builder<Integer, String>();
        final Matcher matcher = Alter.MACROS.matcher(this.value);
        while (matcher.find()) {
            args.put(Integer.valueOf(matcher.group(1)), matcher.group(2));
        }
        return args.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Variable<String> var) {
        return this.value.compareTo(var.asText());
    }

}
