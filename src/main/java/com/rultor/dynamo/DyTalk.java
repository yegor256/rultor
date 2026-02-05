/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.dynamo;

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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.cactoos.list.ListOf;
import org.w3c.dom.Node;
import org.xembly.Directive;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;

/**
 * Talk in Dynamo.
 *
 * @since 1.0
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
        return Long.parseLong(this.item.get(DyTalks.ATTR_NUMBER).n());
    }

    @Override
    public String name() throws IOException {
        return this.item.get(DyTalks.HASH).s();
    }

    @Override
    public Date updated() throws IOException {
        return new Date(
            Long.parseLong(this.item.get(DyTalks.ATTR_UPDATED).n())
        );
    }

    @Override
    public XML read() throws IOException {
        final String xml;
        if (this.item.has(DyTalks.ATTR_XML_ZIP)) {
            xml = DyTalk.unzip(
                this.item.get(DyTalks.ATTR_XML_ZIP).b().asByteArray()
            );
        } else {
            xml = this.item.get(DyTalks.ATTR_XML).s();
        }
        return new StrictXML(
            Talk.UPGRADE.transform(new XMLDocument(xml)),
            Talk.SCHEMA
        );
    }

    @Override
    public void modify(final Iterable<Directive> dirs) throws IOException {
        if (!new ListOf<>(dirs).isEmpty()) {
            final XML xml = this.read();
            final Node node = xml.inner();
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
                        this.item.get(DyTalks.HASH).s()
                    )
                );
            }
            final AttributeValue value = AttributeValue.builder()
                .b(SdkBytes.fromByteArray(body))
                .build();
            this.item.put(
                new AttributeUpdates()
                    .with(DyTalks.ATTR_UPDATED, System.currentTimeMillis())
                    .with(
                        DyTalks.ATTR_XML_ZIP,
                        AttributeValueUpdate.builder()
                            .value(value)
                            .action(AttributeAction.PUT)
                            .build()
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
            IOUtils.toInputStream(xml, StandardCharsets.UTF_8),
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
        return baos.toString(StandardCharsets.UTF_8);
    }

}
