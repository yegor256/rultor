/**
 * Copyright (c) 2009-2018, rultor.com
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

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Item;
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.rultor.spi.Talk;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.cactoos.list.SolidList;
import org.w3c.dom.Node;
import org.xembly.Directive;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Talk in Dynamo.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "item")
public final class DyTalk implements Talk {

    /**
     * Maximum amount of bytes per item in DynamoDB (400Kb).
     * @checkstyle LineLengthCheck (1 line)
     * see <a href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html">link</a>
     * @checkstyle MagicNumber (3 lines)
     */
    private static final int LIMIT = 399 << 10;

    /**
     * UTF-8.
     */
    private static final String UTF_8 = "UTF-8";

    /**
     * Item.
     */
    private final transient Item item;

    /**
     * Ctor.
     * @param itm Item
     */
    DyTalk(final Item itm) {
        this.item = itm;
    }

    @Override
    public Long number() throws IOException {
        return Long.parseLong(this.item.get(DyTalks.ATTR_NUMBER).getN());
    }

    @Override
    public String name() throws IOException {
        return this.item.get(DyTalks.HASH).getS();
    }

    @Override
    public Date updated() throws IOException {
        return new Date(
            Long.parseLong(this.item.get(DyTalks.ATTR_UPDATED).getN())
        );
    }

    @Override
    public XML read() throws IOException {
        final String xml;
        if (this.item.has(DyTalks.ATTR_XML_ZIP)) {
            xml = DyTalk.unzip(
                this.item.get(DyTalks.ATTR_XML_ZIP).getB().array()
            );
        } else {
            xml = this.item.get(DyTalks.ATTR_XML).getS();
        }
        return new StrictXML(
            Talk.UPGRADE.transform(new XMLDocument(xml)),
            Talk.SCHEMA
        );
    }

    @Override
    public void modify(final Iterable<Directive> dirs) throws IOException {
        if (!new SolidList<>(dirs).isEmpty()) {
            final XML xml = this.read();
            final Node node = xml.node();
            try {
                new Xembler(dirs).apply(node);
            } catch (final ImpossibleModificationException ex) {
                throw new IllegalStateException(
                    String.format(
                        "failed to apply %s to %s",
                        dirs.toString(), xml
                    ),
                    ex
                );
            }
            final byte[] body = DyTalk.zip(
                XSLDocument.STRIP.transform(
                    new StrictXML(new XMLDocument(node), Talk.SCHEMA)
                ).toString()
            );
            if (body.length > DyTalk.LIMIT) {
                throw new IllegalArgumentException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "XML is too big (%d bytes, maximum is %d), even after ZIP, in \"%s\"",
                        body.length, DyTalk.LIMIT,
                        this.item.get(DyTalks.HASH).getS()
                    )
                );
            }
            final AttributeValue value = new AttributeValue();
            value.setB(ByteBuffer.wrap(body));
            this.item.put(
                new AttributeUpdates()
                    .with(DyTalks.ATTR_UPDATED, System.currentTimeMillis())
                    .with(
                        DyTalks.ATTR_XML_ZIP,
                        new AttributeValueUpdate(value, AttributeAction.PUT)
                    )
            );
        }
    }

    @Override
    public void active(final boolean yes) throws IOException {
        this.item.put(
            new AttributeUpdates()
                .with(DyTalks.ATTR_ACTIVE, yes)
                .with(DyTalks.ATTR_UPDATED, System.currentTimeMillis())
        );
    }

    /**
     * Zip the XML.
     * @param xml The XML content
     * @return Zipped content
     * @throws IOException If fails
     */
    private static byte[] zip(final String xml) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final OutputStream output = new GZIPOutputStream(baos);
        IOUtils.copy(
            IOUtils.toInputStream(xml, Charset.forName(UTF_8)),
            output
        );
        output.close();
        return baos.toByteArray();
    }

    /**
     * Unzip the XML.
     * @param bytes The XML content
     * @return Unzipped content
     * @throws IOException If fails
     */
    private static String unzip(final byte[] bytes) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(
            new GZIPInputStream(new ByteArrayInputStream(bytes)),
            baos
        );
        return new String(baos.toByteArray(), Charset.forName(UTF_8));
    }

}
