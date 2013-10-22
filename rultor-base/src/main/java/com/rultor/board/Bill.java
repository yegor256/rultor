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
package com.rultor.board;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.rultor.snapshot.Radar;
import com.rultor.snapshot.XemblyException;
import java.util.Collection;
import java.util.Collections;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.SyntaxException;

/**
 * Bill to post on billboard.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Bill {

    /**
     * Get subject.
     * @return Subject line
     */
    String subject();

    /**
     * Get body.
     * @return Body
     */
    String body();

    /**
     * Sender.
     * @return Who sends
     */
    String sender();

    /**
     * Recipients.
     * @return Who receives
     */
    Collection<String> recipients();

    /**
     * Simple bill, from snapshot.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "subj", "from", "rcpts" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements Bill {
        /**
         * Subject line.
         */
        private final transient String subj;
        /**
         * Sender.
         */
        private final transient String from;
        /**
         * Recipients.
         */
        private final transient Array<String> rcpts;
        /**
         * Public ctor.
         * @param subject Subject line
         * @param sender Sender
         * @param recipients Recipients
         */
        public Simple(final String subject, final String sender,
            final Collection<String> recipients) {
            this.subj = subject;
            this.rcpts = new Array<String>(recipients);
            this.from = sender;
        }
        @Override
        public String body() {
            try {
                return new Radar().snapshot().xml().toString();
            } catch (SyntaxException ex) {
                throw new IllegalStateException(ex);
            } catch (XemblyException ex) {
                throw new IllegalStateException(ex);
            }
        }
        @Override
        public String subject() {
            return this.subj;
        }
        @Override
        public String sender() {
            return this.from;
        }
        @Override
        public Collection<String> recipients() {
            return Collections.unmodifiableCollection(this.rcpts);
        }
    }

}
