/**
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.cached;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;

/**
 * Cached talk.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.51
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class CdTalk implements Talk {
    /**
     * Origin talk.
     */
    private final transient Talk origin;

    /**
     * Ctor.
     * @param talk Talks
     */
    CdTalk(final Talk talk) {
        this.origin = talk;
    }

    @Override
    @Cacheable
    public Long number() throws IOException {
        return this.origin.number();
    }

    @Override
    @Cacheable
    public String name() throws IOException {
        return this.origin.name();
    }

    @Override
    @Cacheable
    public Date updated() throws IOException {
        return this.origin.updated();
    }

    @Override
    @Cacheable
    public XML read() throws IOException {
        return this.origin.read();
    }

    @Override
    @Cacheable.FlushBefore
    public void modify(final Iterable<Directive> dirs) throws IOException {
        this.origin.modify(dirs);
    }

    @Override
    @Cacheable.FlushBefore
    public void active(final boolean yes) throws IOException {
        this.origin.active(yes);
    }

}
