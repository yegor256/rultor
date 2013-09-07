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
import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import java.util.ArrayList;
import java.util.Arrays;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
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
     * Coordinates.
     */
    private final transient Coordinates wrk;

    /**
     * Wallet.
     */
    private final transient Wallet wlt;

    /**
     * Ordered values.
     */
    private final transient Array<Object> values;

    /**
     * Public ctor.
     * @param work Coordinates we're in
     * @param wallet Wallet with money
     */
    public Arguments(final Coordinates work, final Wallet wallet) {
        this(work, wallet, new ArrayList<Object>(0));
    }

    /**
     * Public ctor.
     * @param work Coordinates we're in
     * @param wallet Wallet with money
     * @param vals Other values
     */
    public Arguments(
        @NotNull(message = "work can't be NULL") final Coordinates work,
        @NotNull(message = "wallet can't be NULL") final Wallet wallet,
        @NotNull(message = "tail is NULL") final Iterable<Object> vals) {
        this.wrk = work;
        this.wlt = wallet;
        this.values = new Array<Object>(Lists.newArrayList(vals));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return StringUtils.join(Iterables.skip(this.values, 1), " and ");
    }

    /**
     * Get work.
     * @return The work
     */
    public Coordinates work() {
        return this.wrk;
    }

    /**
     * Get wallet.
     * @return The wallet
     */
    public Wallet wallet() {
        return this.wlt;
    }

    /**
     * Get value by position.
     * @param pos Position
     * @return The value
     * @throws SpecException If fails to find it
     */
    public Object get(final int pos) throws SpecException {
        Validate.isTrue(pos >= 0, "position can't be negative");
        if (pos >= this.values.size()) {
            throw new SpecException(
                String.format(
                    "argument #%d is out of bounds (%d total)",
                    pos, this.values.size()
                )
            );
        }
        return this.values.get(pos);
    }

    /**
     * Replace one item.
     * @param pos Position
     * @param value The value to save instead
     * @return New arguments
     */
    public Arguments with(final int pos, final Object value) {
        Validate.isTrue(
            pos <= this.values.size(),
            "argument #%d is out of boundary (%d total)",
            pos, this.values.size()
        );
        return new Arguments(
            this.wrk,
            this.wlt,
            Iterables.concat(
                Iterables.limit(this.values, pos),
                Arrays.asList(value),
                Iterables.skip(this.values, pos + 1)
            )
        );
    }

}
