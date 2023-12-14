/**
 * Copyright (c) 2009-2023 Yegor Bugayenko
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
import com.jcabi.immutable.ArrayMap;
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSD;
import com.jcabi.xml.XSDDocument;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Profile.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Profile {

    /**
     * Schema.
     */
    XSD SCHEMA = XSDDocument.make(
        Talk.class.getResourceAsStream("profile.xsd")
    );

    /**
     * Empty.
     */
    Profile EMPTY = new Profile.Fixed();

    /**
     * Name of the repo.
     * @return Name
     * @since 1.36
     */
    String name();

    /**
     * Name of the branch.
     * @return Name
     * @since 1.5
     */
    String defaultBranch();

    /**
     * Get it in XML format (throws
     * {@link Profile.ConfigException}, if fails).
     *
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
     * If can't read profile due to syntax error.
     */
    final class ConfigException extends RuntimeException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -3860028281726793988L;

        /**
         * Ctor.
         * @param cause Cause of it
         */
        public ConfigException(final String cause) {
            super(cause);
        }

        /**
         * Ctor.
         * @param cause Cause of it
         */
        public ConfigException(final Exception cause) {
            super(cause);
        }
    }

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
         * @return Value
         * @throws IOException If fails
         */
        public String text(final String xpath) throws IOException {
            return this.text(xpath, "");
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
            final String path = String.format("%s/text()", xpath);
            final String text;
            if (xml.nodes(path).isEmpty()) {
                text = def;
            } else {
                text = xml.xpath(path).get(0);
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
         * Name of it.
         */
        private final transient String label;

        /**
         * Name of the branch.
         */
        private final transient String branch;

        /**
         * Ctor.
         */
        public Fixed() {
            this(
                "<p><entry key='merge'/>",
                "<entry key='release'/>",
                "<entry key='deploy'/></p>"
            );
        }

        /**
         * Ctor.
         * @param lines Xml lines
         */
        public Fixed(final String... lines) {
            this(new XMLDocument(String.join("", lines)));
        }

        /**
         * Ctor.
         * @param doc Document
         */
        public Fixed(final XML doc) {
            this(new StrictXML(doc, Profile.SCHEMA), "test/test");
        }

        /**
         * Ctor.
         * @param doc Document
         * @param name Name
         */
        public Fixed(final XML doc, final String name) {
            this(new StrictXML(doc, Profile.SCHEMA), name, "master");
        }

        /**
         * Ctor.
         * @param doc Document
         * @param name Name
         * @param brnch Branch
         */
        public Fixed(final XML doc, final String name, final String brnch) {
            this.xml = doc;
            this.label = name;
            this.branch = brnch;
        }

        @Override
        public String name() {
            return this.label;
        }

        @Override
        public String defaultBranch() {
            return this.branch;
        }

        @Override
        public XML read() {
            return this.xml;
        }

        @Override
        public Map<String, InputStream> assets() {
            return new ArrayMap<>();
        }
    }
}
