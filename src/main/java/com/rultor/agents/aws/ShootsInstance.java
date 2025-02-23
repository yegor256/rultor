/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import java.util.Date;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Terminates EC2 instance if it's older than X hours, but
 * the work hasn't been started yet and the shell is not registered.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class ShootsInstance extends AbstractAgent {

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Maximum age to tolerate, in milliseconds.
     */
    private final transient long max;

    /**
     * Ctor.
     * @param aws API
     * @param msec Max age in millis
     */
    public ShootsInstance(final AwsEc2 aws, final long msec) {
        super(
            "/talk/ec2/instance",
            "/talk/ec2/host",
            "/talk[not(shell)]"
        );
        this.api = aws;
        this.max = msec;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/instance/text()").get(0);
        final long age = new Date().getTime() - this.api.aws()
            .describeInstances(
                new DescribeInstancesRequest().withInstanceIds(instance)
            )
            .getReservations().get(0)
            .getInstances().get(0)
            .getLaunchTime().getTime();
        if (age > this.max) {
            this.api.aws().terminateInstances(
                new TerminateInstancesRequest()
                    .withInstanceIds(instance)
            );
            Logger.warn(
                this,
                "Terminated AWS instance %s because it's %[ms]s old (most probably dead)",
                instance, age
            );
        }
        return new Directives();
    }
}
