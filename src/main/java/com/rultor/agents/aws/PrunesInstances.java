/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.agents.aws;

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
import java.util.Date;
import java.util.concurrent.TimeUnit;
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
     * Ctor.
     * @param aws API
     */
    public PrunesInstances(final AwsEc2 aws) {
        this.api = aws;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        final DescribeInstancesResult res = this.api.aws().describeInstances(
            new DescribeInstancesRequest()
                .withFilters(new Filter().withName("rultor-talk"))
        );
        final long threshold = new Date().getTime() - TimeUnit.HOURS.toMillis(6L);
        for (final Reservation rsrv : res.getReservations()) {
            final Instance instance = rsrv.getInstances().get(0);
            final Date time = instance.getLaunchTime();
            if (time.getTime() > threshold) {
                continue;
            }
            this.api.aws().terminateInstances(
                new TerminateInstancesRequest()
                    .withInstanceIds(instance.getInstanceId())
            );
            Logger.warn(
                this, "AWS instance %s is too old, terminated",
                instance.getInstanceId()
            );
        }
    }

}
