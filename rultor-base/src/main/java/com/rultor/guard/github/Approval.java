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
package com.rultor.guard.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.egit.github.core.PullRequest;

/**
 * Approval for a pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Approval {

    /**
     * This pull request has an approval?
     * @param request Pull request
     * @param client Client
     * @param repo Repository
     * @return TRUE if approved
     * @throws IOException If fails
     */
    boolean has(PullRequest request, Github client, Github.Repo repo)
        throws IOException;

    /**
     * Always yes.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Always implements Approval {
        @Override
        public boolean has(final PullRequest request, final Github client,
            final Github.Repo repo) throws IOException {
            return true;
        }
    }

    /**
     * Always no.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Never implements Approval {
        @Override
        public boolean has(final PullRequest request, final Github client,
            final Github.Repo repo) throws IOException {
            return false;
        }
    }

}
