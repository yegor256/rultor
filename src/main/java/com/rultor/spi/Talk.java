/**
 * Copyright (c) 2009-2019, Yegor Bugayenko
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
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSD;
import com.jcabi.xml.XSDDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLChain;
import com.jcabi.xml.XSLDocument;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xembly.Directive;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Talk.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Talk {

    /**
     * Test name.
     */
    String TEST_NAME = "test";

    /**
     * Schema.
     */
    XSD SCHEMA = XSDDocument.make(
        Talk.class.getResourceAsStream("talk.xsd")
    );

    /**
     * Upgrade XSL.
     */
    XSL UPGRADE = new XSLChain(
        Arrays.asList(
            XSLDocument.make(
                Talk.class.getResourceAsStream(
                    "upgrade/001-talks.xsl"
                )
            ),
            XSLDocument.make(
                Talk.class.getResourceAsStream(
                    "upgrade/002-public-attribute.xsl"
                )
            )
        )
    );

    /**
     * Its unique number.
     * @return Its number
     * @throws IOException If fails
     * @since 1.3
     */
    Long number() throws IOException;

    /**
     * Its unique name.
     * @return Its name
     * @throws IOException If fails
     */
    String name() throws IOException;

    /**
     * When was it updated.
     * @return When
     * @throws IOException If fails
     */
    Date updated() throws IOException;

    /**
     * Read its content.
     * @return Content
     * @throws IOException If fails
     */
    XML read() throws IOException;

    /**
     * Modify its content.
     * @param dirs Directives
     * @throws IOException If fails
     */
    void modify(Iterable<Directive> dirs) throws IOException;

    /**
     * Make it active or passive.
     * @param yes TRUE if it should be active
     * @throws IOException If fails
     */
    void active(boolean yes) throws IOException;

    /**
     * In file.
     */
    @Immutable
    final class InFile implements Talk {
        /**
         * File.
         */
        private final transient String path;
        /**
         * Ctor.
         * @throws IOException If fails
         */
        public InFile() throws IOException {
            this(File.createTempFile("rultor", ".talk"));
            FileUtils.write(
                new File(this.path),
                String.format("<talk name='%s' number='1'/>", Talk.TEST_NAME)
            );
        }
        /**
         * Ctor.
         * @param lines Lines to concat
         * @throws IOException If fails
         */
        public InFile(final String... lines) throws IOException {
            this(new XMLDocument(StringUtils.join(lines)));
        }
        /**
         * Ctor.
         * @param xml XML to save
         * @throws IOException If fails
         */
        public InFile(final XML xml) throws IOException {
            this();
            FileUtils.write(
                new File(this.path),
                new StrictXML(xml, Talk.SCHEMA).toString(),
                CharEncoding.UTF_8
            );
        }
        /**
         * Ctor.
         * @param file The file
         */
        public InFile(final File file) {
            this.path = file.getAbsolutePath();
        }
        @Override
        public Long number() throws IOException {
            return Long.parseLong(this.read().xpath("/talk/@number").get(0));
        }
        @Override
        public String name() throws IOException {
            return this.read().xpath("/talk/@name").get(0);
        }
        @Override
        public Date updated() {
            return new Date(new File(this.path).lastModified());
        }
        @Override
        public XML read() throws IOException {
            return Talk.UPGRADE.transform(
                new XMLDocument(
                    FileUtils.readFileToString(
                        new File(this.path), CharEncoding.UTF_8
                    )
                )
            );
        }
        @Override
        public void modify(final Iterable<Directive> dirs) throws IOException {
            if (dirs.iterator().hasNext()) {
                final Node node = this.read().node();
                try {
                    new Xembler(dirs).apply(node);
                } catch (final ImpossibleModificationException ex) {
                    throw new IllegalStateException(ex);
                }
                FileUtils.write(
                    new File(this.path),
                    new StrictXML(
                        new XMLDocument(node), Talk.SCHEMA
                    ).toString(),
                    CharEncoding.UTF_8
                );
            }
        }
        @Override
        public void active(final boolean yes) {
            // nothing
        }
    }

}
