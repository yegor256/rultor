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
package com.rultor.dynamo;

import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.spi.Assets;
import com.rultor.spi.Repos;
import com.rultor.spi.User;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base in Dynamo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class DyUser implements User {

    /**
     * Region to work with.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN name;

    /**
     * Ctor.
     * @param reg Region
     * @param urn Name of the user (URN)
     */
    DyUser(final Region reg, final URN urn) {
        this.region = reg;
        this.name = urn;
    }

    @Override
    public URN urn() {
        return this.name;
    }

    @Override
    public Repos repos() {
        return new DyRepos(this.region, this.name);
    }

    @Override
    public Assets assets() {
        throw new UnsupportedOperationException("#assets()");
    }

}
