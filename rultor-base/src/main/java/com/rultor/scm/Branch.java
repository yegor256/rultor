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

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.validation.constraints.NotNull;

/**
 * SCM Branch.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Branch {

    /**
     * SCM it's in.
     * @return The SCM
     */
    @NotNull
    SCM scm();

    /**
     * Name of it.
     * @return The name
     */
    @NotNull
    String name();

    /**
     * Get history of commits, the latest on top.
     * @return Commits
     * @throws IOException If fails
     */
    @NotNull
    Iterable<Commit> log() throws IOException;

    /**
     * Passive branch, without any active components.
     */
    final class Passive implements Branch {
        /**
         * URL of SCM.
         */
        private final transient String addr;
        /**
         * Branch name.
         */
        private final transient String label;
        /**
         * Public ctor.
         * @param url URL of SCM
         * @param name Name of the branch
         */
        public Passive(final URL url, final String name) {
            this.addr = url.toString();
            this.label = name;
        }
        @Override
        public SCM scm() {
            return new SCM() {
                @Override
                public URL url() {
                    try {
                        return new URL(Passive.this.addr);
                    } catch (MalformedURLException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                @Override
                public Branch checkout(final String name) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterable<String> branches() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        @Override
        public String name() {
            return Passive.this.label;
        }
        @Override
        public Iterable<Commit> log() {
            throw new UnsupportedOperationException();
        }
    }

}
