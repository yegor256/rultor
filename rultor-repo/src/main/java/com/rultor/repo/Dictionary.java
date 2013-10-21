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
import com.jcabi.immutable.ArrayMap;
import com.rultor.spi.Arguments;
import com.rultor.spi.SpecException;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Array.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "map")
@Loggable(Loggable.DEBUG)
final class Dictionary implements Variable<Map<String, Object>> {

    /**
     * Map of values.
     */
    private final transient ArrayMap<Variable<String>, Variable<?>> map;

    /**
     * Public ctor.
     * @param vals Values
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected Dictionary(final Map<Variable<String>, Variable<?>> vals) {
        this.map = new ArrayMap<Variable<String>, Variable<?>>(vals);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (8 lines)
     */
    @Override
    @NotNull
    public Map<String, Object> instantiate(
        @NotNull(message = "users can't be NULL") final Users users,
        @NotNull(message = "arguments can't be NULL") final Arguments args)
        throws SpecException {
        final ConcurrentMap<String, Object> objects =
            new ConcurrentHashMap<String, Object>(this.map.size());
        for (Map.Entry<Variable<String>, Variable<?>> pair
            : this.map.entrySet()) {
            objects.put(
                pair.getKey().instantiate(users, args),
                pair.getValue().instantiate(users, args)
            );
        }
        return objects;
    }

    @Override
    public String asText() {
        return new StringBuilder()
            .append('{')
            .append(this.brackets())
            .append('}')
            .toString();
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Map<Integer, String> arguments() throws SpecException {
        final ConcurrentMap<Integer, String> args =
            new ConcurrentSkipListMap<Integer, String>();
        for (Variable<?> var : this.map.values()) {
            args.putAll(var.arguments());
        }
        return args;
    }

    /**
     * Return brackets.
     * @return Brackets
     */
    private Brackets<?> brackets() {
        return new Brackets<Map.Entry<Variable<String>, Variable<?>>>(
            this.map.entrySet(),
            new Brackets.Format<Map.Entry<Variable<String>, Variable<?>>>() {
                @Override
                public String print(
                    final Map.Entry<Variable<String>, Variable<?>> entry) {
                    return String.format(
                        "%s: %s",
                        entry.getKey().asText(),
                        entry.getValue().asText()
                    );
                }
            }
        );
    }

}
