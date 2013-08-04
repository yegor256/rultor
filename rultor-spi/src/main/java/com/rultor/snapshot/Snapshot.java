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
package com.rultor.snapshot;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.Document;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;
import org.xembly.XemblySyntaxException;

/**
 * Snapshot.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Snapshot {

    /**
     * Print it as a Xembly document.
     * @return Xembly script
     */
    @NotNull(message = "output Xembly is never NULL")
    String xembly();

    /**
     * Xembly to document formatter.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "xembly")
    @Loggable(Loggable.DEBUG)
    final class XML {
        /**
         * Script.
         */
        private final transient String xembly;
        /**
         * Public ctor.
         * @param snapshot Snapshot
         */
        public XML(final Snapshot snapshot) {
            this(snapshot.xembly());
        }
        /**
         * Public ctor.
         * @param script Script
         */
        public XML(final String script) {
            this.xembly = script;
        }
        /**
         * Convert it to DOM document.
         * @return DOM document
         */
        public Document dom() {
            final Document dom;
            try {
                dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            } catch (ParserConfigurationException ex) {
                throw new IllegalStateException(ex);
            }
            dom.appendChild(dom.createElement("snapshot"));
            try {
                new Xembler(new Directives(this.xembly)).exec(dom);
            } catch (XemblySyntaxException ex) {
                throw new IllegalArgumentException(ex);
            } catch (ImpossibleModificationException ex) {
                throw new IllegalArgumentException(ex);
            }
            return dom;
        }
    }

}
