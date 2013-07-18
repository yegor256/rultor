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
package com.rultor.spi;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * Arguments for {@link Variable} instantiation.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "values")
public final class Arguments {

    /**
     * Ordered values.
     */
    private final transient Object[] values;

    /**
     * Public ctor.
     * @param work Mandatory first value
     */
    public Arguments(final Work work) {
        this(work, new ArrayList<Object>(0));
    }

    /**
     * Public ctor.
     * @param work Mandatory first value
     * @param tail Other values
     */
    public Arguments(@NotNull(message = "work can't be NULL") final Work work,
        @NotNull(message = "tail is NULL") final Collection<Object> tail) {
        this(Iterables.concat(Arrays.asList(work), tail));
    }

    /**
     * Public ctor.
     * @param vals All values
     */
    private Arguments(final Iterable<Object> vals) {
        this.values = Iterables.toArray(vals, Object.class);
    }

    /**
     * Get value by position.
     * @param pos Position
     * @return The value
     * @throws SpecException If fails to find it
     */
    public Object get(final int pos) throws SpecException {
        Validate.isTrue(pos >= 0, "position can't be negative");
        if (pos >= this.values.length) {
            throw new SpecException(String.format("#%d is out of bounds", pos));
        }
        return this.values[pos];
    }

    /**
     * Get them all as objects.
     * @return Objects
     */
    public Collection<Object> get() {
        return Arrays.asList(this.values);
    }

    /**
     * Replace one item.
     * @param pos Position
     * @param value The value to save instead
     * @return New arguments
     */
    public Arguments with(final int pos, final Object value) {
        Validate.isTrue(pos < this.values.length, "%d is out of boundary", pos);
        return new Arguments(
            Iterables.concat(
                Iterables.limit(Arrays.asList(this.values), pos),
                Arrays.asList(value),
                Iterables.skip(Arrays.asList(this.values), pos + 1)
            )
        );
    }

}
