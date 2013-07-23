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
import com.jcabi.immutable.Array;
import com.rultor.spi.Variable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Brackets.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "vars")
@Loggable(Loggable.DEBUG)
final class Brackets {

    /**
     * Indentation.
     */
    private static final String INDENT = "  ";

    /**
     * EOL.
     */
    private static final String EOL = "\n";

    /**
     * Variables to render.
     */
    private final transient Array<Variable<?>> vars;

    /**
     * Transition function.
     */
    private final transient Brackets.Format format;

    /**
     * Formatter.
     */
    @Immutable
    public interface Format {
        /**
         * Format the line.
         * @param pos Position of the variable
         * @param var Variable to render
         * @return Text rendered
         */
        String print(int pos, Variable<?> var);
    }

    /**
     * Public ctor.
     * @param args Arguments
     */
    protected Brackets(final Collection<Variable<?>> args) {
        this(
            args,
            new Brackets.Format() {
                @Override
                public String print(final int pos, final Variable<?> var) {
                    return var.asText();
                }
            }
        );
    }

    /**
     * Public ctor.
     * @param args Arguments
     * @param fmt Format to use for printing
     */
    protected Brackets(final Collection<Variable<?>> args, final Brackets.Format fmt) {
        this.vars = new com.jcabi.immutable.Array<Variable<?>>(args);
        this.format = fmt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        final List<String> kids = new ArrayList<String>(this.vars.size());
        for (int pos = 0; pos < this.vars.size(); ++pos) {
            kids.add(this.format.print(pos, this.vars.get(pos)));
        }
        final String line = StringUtils.join(kids, ", ");
        if (line.length() < Tv.FIFTY && !line.contains(Brackets.EOL)) {
            text.append(line);
        } else {
            final String shift = new StringBuilder()
                .append(CharUtils.LF).append(Brackets.INDENT).toString();
            int idx;
            for (idx = 0; idx < kids.size(); ++idx) {
                if (idx > 0) {
                    text.append(',');
                }
                text.append(shift)
                    .append(kids.get(idx).replace(Brackets.EOL, shift));
            }
            if (idx > 0) {
                text.append(CharUtils.LF);
            }
        }
        return text.toString();
    }

}
