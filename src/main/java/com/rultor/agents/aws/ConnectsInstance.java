/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.PfShell;
import java.io.IOException;
import java.util.Date;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Connects a running EC2 instance: detects its IP.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class ConnectsInstance extends AbstractAgent {

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Shell.
     */
    private final transient PfShell shell;

    /**
     * Ctor.
     * @param aws API
     * @param shll The shell
     */
    public ConnectsInstance(final AwsEc2 aws, final PfShell shll) {
        super(
            "/talk[daemon and not(shell)]",
            "/talk/ec2/instance",
            "/talk/ec2/host"
        );
        this.api = aws;
        this.shell = shll;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/instance/text()").get(0);
        final String host = xml.xpath("/talk/ec2/host/text()").get(0);
        final String name = xml.xpath("/talk/@name").get(0);
        final Directives dirs = new Directives();
        if (this.alive(host)) {
            Logger.warn(
                this, "AWS instance %s is alive at %s for %s",
                instance, host, name
            );
            dirs.xpath("/talk").add("shell")
                .attr("id", xml.xpath("/talk/daemon/@id").get(0))
                .add("host").set(host).up()
                .add("port").set(Integer.toString(this.shell.port())).up()
                .add("login").set(this.shell.login()).up()
                .add("key").set(this.shell.key());
            Logger.info(
                this, "AWS instance %s launched for %s and running at %s",
                instance, name, host
            );
        } else {
            final long age = new Date().getTime() - this.api.aws()
                .describeInstances(
                    new DescribeInstancesRequest().withInstanceIds(instance)
                )
                .getReservations().get(0)
                .getInstances().get(0)
                .getLaunchTime().getTime();
            final String status = this.api.aws().describeInstanceStatus(
                new DescribeInstanceStatusRequest()
                    .withIncludeAllInstances(true)
                    .withInstanceIds(instance)
            ).getInstanceStatuses().get(0).getInstanceState().getName();
            Logger.warn(
                this, "Can't connect %s to AWS instance %s at %s (%[ms]s old, \"%s\")",
                name, instance, host, age, status
            );
        }
        return dirs;
    }

    /**
     * Tries to connect to it via SSH and returns TRUE if it's possible.
     * @param host IP of the host
     * @return TRUE if alive
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private boolean alive(final String host) {
        boolean alive = false;
        try {
            new Shell.Empty(
                new Shell.Safe(
                    this.shell.withHost(host).toSsh()
                )
            ).exec("whoami");
            alive = true;
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception ex) {
            Logger.warn(
                this, "Failed to SSH-connect to %s: %s",
                host, ex.getMessage()
            );
        }
        return alive;
    }
}
