/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;

/**
 * Finds IP of a running EC2 instance.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class DescribesInstance extends AbstractAgent {

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Ctor.
     * @param aws API
     */
    public DescribesInstance(final AwsEc2 aws) {
        super(
            "/talk[daemon]",
            "/talk/ec2[not(host)]",
            "/talk/ec2/instance"
        );
        this.api = aws;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/instance/text()").get(0);
        final Directives dirs = new Directives();
        try (Ec2Client client = this.api.aws()) {
            final String state = client.describeInstanceStatus(
                DescribeInstanceStatusRequest.builder()
                    .includeAllInstances(true)
                    .instanceIds(instance)
                    .build()
            ).instanceStatuses().get(0).instanceState().nameAsString();
            Logger.info(this, "AWS instance %s state: %s", instance, state);
            if ("running".equals(state)) {
                final Instance ready = client.describeInstances(
                    DescribeInstancesRequest.builder()
                        .instanceIds(instance)
                        .build()
                ).reservations().get(0).instances().get(0);
                final String host = ready.publicIpAddress();
                dirs.xpath("/talk/ec2").add("host").set(host);
                Logger.info(this, "AWS instance %s is at %s", instance, host);
            }
        }
        return dirs;
    }

}
