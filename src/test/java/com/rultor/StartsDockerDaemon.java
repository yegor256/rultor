/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;

/**
 * Starts a Docker Container containing a Docker daemon and SSHD.
 *
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.63
 */
public final class StartsDockerDaemon implements AutoCloseable {

    /**
     * Profile used.
     */
    private final transient Profile profile;

    /**
     * Docker client.
     */
    private final transient DockerClient client;

    /**
     * Docker Containers created.
     */
    private final transient Collection<CreateContainerResponse> containers;

    /**
     * Ctor.
     * @param prof Current Profile
     */
    public StartsDockerDaemon(final Profile prof) {
        this.profile = prof;
        this.client = DockerClientBuilder.getInstance().build();
        this.containers = Collections.newSetFromMap(
            // @checkstyle MagicNumber (1 line)
            new ConcurrentHashMap<>(1, 0.9f, 1)
        );
    }

    /**
     * Sets up a PfShell in which Rultor can function.
     * @return PfShell loaded with credentials for new Rultor runner container
     * @throws IOException on failure
     */
    public PfShell shell() throws IOException {
        final ExposedPort ssh = ExposedPort.tcp(22);
        final Ports ports = new Ports();
        ports.bind(ssh, Ports.Binding.empty());
        final CreateContainerResponse container = this.client
            .createContainerCmd(this.build())
            .withExposedPorts(ssh)
            .exec();
        this.containers.add(container);
        this.client.startContainerCmd(container.getId()).exec();
        return new PfShell(
            this.profile,
            this.client.infoCmd().exec().getName(),
            this.client.inspectContainerCmd(container.getId())
                .exec().getNetworkSettings().getPorts().getBindings().keySet()
                .iterator().next().getPort(),
            "root",
            this.key(container)
        );
    }

    @Override
    public void close() throws IOException {
        for (final CreateContainerResponse container : this.containers) {
            this.client.killContainerCmd(container.getId()).exec();
            this.client.removeContainerCmd(container.getId()).exec();
        }
        this.client.close();
    }

    /**
     * Retrieves SSH private key needed to connect to a given container.
     * @param container Container from which to get the SSH key
     * @return SSH private key
     * @throws IOException on failure
     */
    private String key(final CreateContainerResponse container)
        throws IOException {
        final StringWriter writer = new StringWriter();
        IOUtils.copy(
            this.client.copyArchiveFromContainerCmd(
                container.getId(), "/root/.ssh/id_rsa"
            ).exec(),
            writer
        );
        final String key = writer.toString();
        return key.substring(key.indexOf('-'), key.lastIndexOf('-') + 1);
    }

    /**
     * Builds a fresh Rultor runner base image on the Docker daemon.
     * @return Image ID of the Rultor runner base image
     */
    private String build() {
        return this.client.buildImageCmd(
            new File(this.getClass().getResource("image").getPath())
        ).exec(new BuildImageResultCallback()).awaitImageId();
    }

}
