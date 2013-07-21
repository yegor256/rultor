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
package com.rultor.base;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Proxy;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * Concatenated strings.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "parts", "separator" })
@Loggable(Loggable.DEBUG)
public final class Concat implements Proxy<String> {

    /**
     * Strings to concatenate.
     */
    private final transient String[] parts;

    /**
     * Separator to use.
     */
    private final transient String separator;

    /**
     * Public ctor.
     * @param texts Lines to concatenate
     */
    public Concat(@NotNull(message = "list of lines can't be NULL")
        final Collection<String> texts) {
        this(texts, "");
    }

    /**
     * Public ctor.
     * @param texts Lines to concatenate
     * @param sep Separator
     */
    public Concat(@NotNull(message = "lines can't be NULL")
        final Collection<String> texts,
        @NotNull(message = "separator can't be NULL") final String sep) {
        this.parts = texts.toArray(new String[texts.size()]);
        this.separator = sep;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String object() {
        return StringUtils.join(this.parts, this.separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%d part(s)", this.parts.length);
    }

}
