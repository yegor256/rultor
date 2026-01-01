/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import lombok.ToString;

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
        final DescribeInstancesResult res = this.api.aws().describeInstances(
            new DescribeInstancesRequest()
                .withFilters(new Filter().withName("tag:rultor").withValues("yes"))
        );
        final Collection<String> seen = new LinkedList<>();
        for (final Reservation rsrv : res.getReservations()) {
            final Instance instance = rsrv.getInstances().get(0);
            final String status = this.api.aws().describeInstanceStatus(
                new DescribeInstanceStatusRequest()
                    .withIncludeAllInstances(true)
                    .withInstanceIds(instance.getInstanceId())
            ).getInstanceStatuses().get(0).getInstanceState().getName();
            final long age = new Date().getTime() - instance.getLaunchTime().getTime();
            final String label = Logger.format(
                "%s/%s/%s/%[ms]s",
                instance.getInstanceId(),
                instance.getInstanceType(),
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
                new TerminateInstancesRequest()
                    .withInstanceIds(instance.getInstanceId())
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
