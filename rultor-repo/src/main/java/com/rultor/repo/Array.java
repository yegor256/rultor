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
import com.jcabi.aspects.Tv;
import com.rultor.spi.SpecException;
import com.rultor.spi.Users;
import com.rultor.spi.Variable;
import com.rultor.spi.Work;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Array.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "values")
@Loggable(Loggable.DEBUG)
final class Array implements Variable<Iterable<Object>> {

    /**
     * Indentation.
     */
    private static final String INDENT = "  ";

    /**
     * EOL.
     */
    private static final String EOL = "\n";

    /**
     * Values.
     */
    private final transient Variable<?>[] values;

    /**
     * Public ctor.
     * @param vals Values
     */
    protected Array(final Collection<Variable<?>> vals) {
        this.values = vals.toArray(new Variable<?>[vals.size()]);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    @NotNull
    public Iterable<Object> instantiate(@NotNull final Users users,
        @NotNull final Work work) throws SpecException {
        final Collection<Object> objects =
            new ArrayList<Object>(this.values.length);
        for (Variable<?> var : this.values) {
            objects.add(var.instantiate(users, work));
        }
        return objects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asText() {
        final StringBuilder text = new StringBuilder();
        text.append('[');
        final List<String> kids = new ArrayList<String>(this.values.length);
        for (Variable<?> var : this.values) {
            kids.add(var.asText());
        }
        final String line = StringUtils.join(kids, ", ");
        if (line.length() < Tv.FIFTY && !line.contains(Array.EOL)) {
            text.append(line);
        } else {
            final String shift = new StringBuilder()
                .append(CharUtils.LF).append(Array.INDENT).toString();
            int idx;
            for (idx = 0; idx < kids.size(); ++idx) {
                if (idx > 0) {
                    text.append(',');
                }
                text.append(shift)
                    .append(kids.get(idx).replace(Array.EOL, shift));
            }
            if (idx > 0) {
                text.append(CharUtils.LF);
            }
        }
        text.append(']');
        return text.toString();
    }

}
