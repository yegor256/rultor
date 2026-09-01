/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Region;
import com.jcabi.s3.retry.ReRegion;
import com.jcabi.xml.XML;
import com.rultor.Env;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * S3 connect.
 * @since 1.1
 */
@Immutable
final class S3Connect implements Connect {

    /**
     * XML of the talk.
     */
    private final transient XML xml;

    /**
     * Hash.
     */
    private final transient String hash;

    /**
     * Ctor.
     * @param talk Talk
     * @param name Name of the archive
     */
    S3Connect(final XML talk, final String name) {
        this.xml = talk;
        this.hash = name;
    }

    @Override
    public InputStream read() throws IOException {
        final URI uri = URI.create(
            this.xml.xpath(
                String.format(
                    "/talk/archive/log[@id='%s']/text()",
                    this.hash
                )
            ).get(0)
        );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        S3Connect.bucket().ocket(uri.getPath().substring(1)).read(
            baos
        );
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private static Bucket bucket() {
        return new ReRegion(
            new Region.Simple(
                Env.read("Rultor-S3Key"),
                Env.read("Rultor-S3Secret")
            )
        ).bucket(Env.read("Rultor-S3Bucket"));
    }
}
