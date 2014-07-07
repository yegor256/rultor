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
package com.rultor.web;

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Region;
import com.jcabi.s3.retry.ReRegion;
import com.jcabi.xml.XML;
import com.rultor.agents.shells.Shell;
import com.rultor.agents.shells.TalkShells;
import com.rultor.spi.Talk;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.input.NullInputStream;

/**
 * Single daemon.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/t/{name:[0-9a-f]+}/{hash:[a-f0-9]+}")
public final class DaemonRs extends BaseRs {

    /**
     * Talk unique name.
     */
    private transient String name;

    /**
     * Daemon hash ID.
     */
    private transient String hash;

    /**
     * Inject it from query.
     * @param talk Talk name
     */
    @PathParam("name")
    public void setName(@NotNull(message = "talk name is mandatory")
        final String talk) {
        this.name = talk;
    }

    /**
     * Inject it from query.
     * @param dmn Daemon hash
     */
    @PathParam("hash")
    public void setHash(@NotNull(message = "daemon hash is mandatory")
        final String dmn) {
        this.hash = dmn;
    }

    /**
     * Get front.
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response index() throws IOException {
        if (!this.talks().exists(this.name)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "there is no such page here",
                Level.WARNING
            );
        }
        return Response.ok().entity(this.stream()).build();
    }

    /**
     * Get stream.
     * @return The stream
     * @throws IOException If fails
     */
    private InputStream stream() throws IOException {
        final Talk talk = this.talks().get(this.name);
        final XML xml = talk.read();
        final File file = File.createTempFile("rultor", ".txt");
        file.deleteOnExit();
        if (!this.ssh(xml, file) && !this.archive(xml, file)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "there is no such log here",
                Level.WARNING
            );
        }
        return new FileInputStream(file);
    }

    /**
     * Read shell if exists.
     * @param xml XML to read
     * @param file File to write
     * @return TRUE if exists
     * @throws IOException If fails
     */
    private boolean ssh(final XML xml, final File file) throws IOException {
        final String xpath = String.format(
            "/talk/daemon[@id='%s']/dir", this.hash
        );
        boolean found = false;
        if (!xml.nodes(xpath).isEmpty()) {
            final Shell shell = new TalkShells(xml).get();
            shell.exec(
                String.format(
                    "cat %s/stdout",
                    xml.xpath("/talk/daemon/dir/text()").get(0)
                ),
                new NullInputStream(0L),
                new FileOutputStream(file),
                Logger.stream(Level.SEVERE, true)
            );
            found = true;
        }
        return found;
    }

    /**
     * Read archive if exists.
     * @param xml XML to read
     * @param file File to write
     * @return TRUE if exists
     * @throws IOException If fails
     */
    private boolean archive(final XML xml, final File file) throws IOException {
        final String xpath = String.format(
            "/talk/archive/log[@id='%s']/text()", this.hash
        );
        boolean found = false;
        if (!xml.nodes(xpath).isEmpty()) {
            final URI uri = URI.create(xml.xpath(xpath).get(0));
            DaemonRs.bucket().ocket(uri.getPath().substring(1)).read(
                new FileOutputStream(file)
            );
            found = true;
        }
        return found;
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
