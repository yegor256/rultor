/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.stateful.sdb;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.simpledb.Domain;
import com.jcabi.simpledb.Region;
import com.rultor.spi.Coordinates;
import com.rultor.stateful.Spinbox;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Spinboxes in Amazon SimpleDB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "work", "domain" })
public final class DomainSpinbox implements Spinbox {

    /**
     * Coordinates we're in.
     */
    private final transient Coordinates work;

    /**
     * SimpleDB client.
     */
    private final transient Domain domain;

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param dmn Domain
     */
    public DomainSpinbox(
        @NotNull(message = "work can't be NULL") final Coordinates wrk,
        @NotNull(message = "domain can't be NULL") final Domain dmn) {
        this.work = wrk;
        this.domain = dmn;
    }

    /**
     * Public ctor.
     * @param wrk Coordinates we're in
     * @param region Region
     * @param name Domain name
     */
    public DomainSpinbox(final Coordinates wrk,
        @NotNull(message = "region can't be NULL") final Region region,
        @NotNull(message = "domain name can't be NULL") final String name) {
        this(wrk, region.domain(name));
    }

    @Override
    public long add(final long value) {
        return new ItemSpinbox(
            this.domain.item(
                String.format(
                    "%s %s %s",
                    this.work.owner(),
                    this.work.rule(),
                    this.work.scheduled()
                )
            )
        ).add(value);
    }

}
