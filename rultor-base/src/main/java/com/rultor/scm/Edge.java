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
package com.rultor.scm;

import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Edge of development (latest branch in the list).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "scm")
@Loggable(Loggable.DEBUG)
public final class Edge implements SCM {

    /**
     * SCM.
     */
    private final transient SCM scm;

    /**
     * Public ctor.
     * @param src SCM
     */
    public Edge(@NotNull(message = "SCM can't be NULL") final SCM src) {
        this.scm = src;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("edge in %s", this.scm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Branch checkout(final String name) throws IOException {
        return this.scm.checkout(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> branches() throws IOException {
        final List<String> branches = new LinkedList<String>();
        final List<String> all = Lists.newLinkedList(this.scm.branches());
        if (!all.isEmpty()) {
            branches.add(all.get(all.size() - 1));
        }
        return branches;
    }

}
