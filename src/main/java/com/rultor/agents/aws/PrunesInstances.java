/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import lombok.ToString;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

/**
 * Terminates all instances that are very old.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class PrunesInstances implements SuperAgent {

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
     * @param msec Maximum age to tolerate
     */
    public PrunesInstances(final AwsEc2 aws, final long msec) {
        this.api = aws;
        this.max = msec;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        final DescribeInstancesResponse res = this.api.aws().describeInstances(
            DescribeInstancesRequest.builder()
                .filters(
                    Filter.builder()
                        .name("tag:rultor")
                        .values("yes")
                        .build()
                )
                .build()
        );
        final Collection<String> seen = new LinkedList<>();
        for (final Reservation rsrv : res.reservations()) {
            final Instance instance = rsrv.instances().get(0);
            final String status = this.api.aws().describeInstanceStatus(
                DescribeInstanceStatusRequest.builder()
                    .includeAllInstances(true)
                    .instanceIds(instance.instanceId())
                    .build()
            ).instanceStatuses().get(0).instanceState().nameAsString();
            final long age = new Date().getTime() - instance.launchTime().toEpochMilli();
            final String label = Logger.format(
                "%s/%s/%s/%[ms]s",
                instance.instanceId(),
                instance.instanceTypeAsString(),
                status, age
            );
            if ("terminated".equals(status)) {
                continue;
            }
            seen.add(label);
            if (age < this.max) {
                continue;
            }
            this.api.aws().terminateInstances(
                TerminateInstancesRequest.builder()
                    .instanceIds(instance.instanceId())
                    .build()
            );
            Logger.warn(
                this, "AWS instance %s is too old, terminated",
                label
            );
        }
        Logger.info(
            this, "Checked %d AWS instances: %[list]s",
            seen.size(), seen
        );
    }

}
