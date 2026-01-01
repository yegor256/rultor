/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;

/**
 * Starts a Docker Container containing a Docker daemon and SSHD.
 *
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
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public StartsDockerDaemon(final Profile prof) {
        this.profile = prof;
        final DefaultDockerClientConfig config =
            DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        this.client = DockerClientBuilder.getInstance(config)
            .withDockerHttpClient(
                new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build()
            )
            .build();
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
            writer,
            StandardCharsets.UTF_8
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
            new File(Objects.requireNonNull(this.getClass().getResource("image")).getPath())
        ).exec(new BuildImageResultCallback()).awaitImageId();
    }

}
