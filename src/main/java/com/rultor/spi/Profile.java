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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Profile.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Profile {

    /**
     * Get it in XML format.
     * @return XML
     * @throws IOException If fails
     */
    XML read() throws IOException;

    /**
     * Get assets.
     * @return Map of assets
     * @throws IOException If fails
     */
    Map<String, InputStream> assets() throws IOException;

    /**
     * Defaults.
     */
    @Immutable
    final class Defaults {
        /**
         * Original profile.
         */
        private final transient Profile origin;
        /**
         * Ctor.
         * @param profile The profile
         */
        public Defaults(final Profile profile) {
            this.origin = profile;
        }
        /**
         * Get text item.
         * @param xpath Path
         * @param def Default, if it's absent
         * @return Value
         * @throws IOException If fails
         */
        public String text(final String xpath, final String def)
            throws IOException {
            final XML xml = this.origin.read();
            final String text;
            if (xml.nodes(xpath).isEmpty()) {
                text = def;
            } else {
                text = xml.xpath(String.format("%s/text()", xpath)).get(0);
            }
            return text;
        }
    }

    /**
     * Fixed.
     */
    @Immutable
    final class Fixed implements Profile {
        /**
         * XML document.
         */
        private final transient XML xml;
        /**
         * Ctor.
         * @param doc Document
         */
        public Fixed(final XML doc) {
            this.xml = doc;
        }
        @Override
        public XML read() throws IOException {
            return this.xml;
        }
        @Override
        public Map<String, InputStream> assets() {
            throw new UnsupportedOperationException("#assets()");
        }
    }
}
