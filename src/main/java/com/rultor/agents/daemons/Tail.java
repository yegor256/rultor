/**
 * Copyright (c) 2009-2017, rultor.com
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
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Region;
import com.jcabi.s3.retry.ReRegion;
import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.agents.shells.TalkShells;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Tail daemon output.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "xml", "hash" })
public final class Tail {

    /**
     * Talk.
     */
    private final transient XML xml;

    /**
     * Hash.
     */
    private final transient String hash;

    /**
     * Ctor.
     * @param talk Talk
     * @param hsh Hash
     */
    public Tail(final XML talk, final String hsh) {
        this.xml = talk;
        this.hash = hsh;
    }

    /**
     * Read it.
     * @return Stream with log
     * @throws IOException If fails
     */
    @SuppressWarnings("unchecked")
    public InputStream read() throws IOException {
        final Collection<Map.Entry<String, Tail.Connect>> connects =
            Arrays.<Map.Entry<String, Tail.Connect>>asList(
                new AbstractMap.SimpleEntry<String, Tail.Connect>(
                    String.format(
                        "/talk/archive/log[@id='%s' and starts-with(.,'s3:')]",
                        this.hash
                    ),
                    new Tail.S3Connect(this.xml, this.hash)
                ),
                new AbstractMap.SimpleEntry<String, Tail.Connect>(
                    String.format(
                        "/talk[shell and daemon[@id='%s'] and daemon/dir]",
                        this.hash
                    ),
                    new Tail.SSHConnect(this.xml)
                ),
                new AbstractMap.SimpleEntry<String, Tail.Connect>(
                    "/talk[daemon[@id='00000000'] and daemon/dir]",
                    new Tail.FakeConnect(this.xml)
                ),
                new AbstractMap.SimpleEntry<String, Tail.Connect>(
                    "/talk",
                    new Tail.Connect() {
                        @Override
                        public InputStream read() {
                            return IOUtils.toInputStream(
                                StringUtils.join(
                                    String.format(
                                        "rultor.com %s/%s\n",
                                        Manifests.read("Rultor-Version"),
                                        Manifests.read("Rultor-Revision")
                                    ),
                                    "nothing yet, try again in 15 seconds"
                                )
                            );
                        }
                    }
                )
            );
        InputStream stream = null;
        for (final Map.Entry<String, Tail.Connect> ent : connects) {
            if (!this.xml.nodes(ent.getKey()).isEmpty()) {
                stream = ent.getValue().read();
                break;
            }
        }
        if (stream == null) {
            throw new IllegalArgumentException("internal error");
        }
        return stream;
    }

    /**
     * Connect to the log.
     */
    @Immutable
    private interface Connect {
        /**
         * Read it.
         * @return Stream
         * @throws IOException If fails
         */
        InputStream read() throws IOException;
    }

    /**
     * S3 connect.
     */
    @Immutable
    private static final class S3Connect implements Tail.Connect {
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
        private S3Connect(final XML talk, final String name) {
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
            Tail.S3Connect.bucket().ocket(uri.getPath().substring(1)).read(
                baos
            );
            return new ByteArrayInputStream(baos.toByteArray());
        }
        /**
         * S3 bucket.
         * @return Bucket
         */
        private static Bucket bucket() {
            return new ReRegion(
                new Region.Simple(
                    Manifests.read("Rultor-S3Key"),
                    Manifests.read("Rultor-S3Secret")
                )
            ).bucket(Manifests.read("Rultor-S3Bucket"));
        }
    }

    /**
     * SSH connect.
     */
    @Immutable
    private static final class SSHConnect implements Tail.Connect {
        /**
         * XML of the talk.
         */
        private final transient XML xml;
        /**
         * Ctor.
         * @param talk Talk
         */
        private SSHConnect(final XML talk) {
            this.xml = talk;
        }
        @Override
        public InputStream read() throws IOException {
            final Shell shell = new TalkShells(this.xml).get();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            shell.exec(
                StringUtils.join(
                    String.format(
                        "dir=%s;",
                        SSH.escape(
                            this.xml.xpath("/talk/daemon/dir/text()").get(0)
                        )
                    ),
                    " (cat \"${dir}/stdout\" 2>/dev/null",
                    " || echo \"file $file is gone\")",
                    " | iconv -f utf-8 -t utf-8 -c",
                    " | LANG=en_US.UTF-8 col -b"
                ),
                new NullInputStream(0L), baos,
                Logger.stream(Level.SEVERE, true)
            );
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }

    /**
     * Fake file connect.
     */
    @Immutable
    private static final class FakeConnect implements Tail.Connect {
        /**
         * XML of the talk.
         */
        private final transient XML xml;
        /**
         * Ctor.
         * @param talk Talk
         */
        private FakeConnect(final XML talk) {
            this.xml = talk;
        }
        @Override
        public InputStream read() throws FileNotFoundException {
            return new FileInputStream(
                new File(this.xml.xpath("/talk/daemon/dir/text() ").get(0))
            );
        }
    }

}
