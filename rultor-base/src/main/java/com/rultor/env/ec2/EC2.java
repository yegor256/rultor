/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.env.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.rultor.env.Environment;
import com.rultor.env.Environments;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Amazon EC2 environments.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class EC2 implements Environments {

    /**
     * Type of EC2 instance.
     */
    private final transient String type;

    /**
     * Name of AMI.
     */
    private final transient String ami;

    /**
     * EC2 client.
     */
    private final transient EC2Client client;

    /**
     * Public ctor.
     * @param tpe Instance type, for example "t1.micro"
     * @param image AMI name
     * @param akey AWS key
     * @param scrt AWS secret
     */
    public EC2(final String tpe, final String image,
        final String akey, final String scrt) {
        this(tpe, image, new EC2Client.Simple(akey, scrt));
    }

    /**
     * Public ctor.
     * @param tpe Instance type, for example "t1.micro"
     * @param image AMI name
     * @param clnt EC2 client
     */
    public EC2(final String tpe, final String image, final EC2Client clnt) {
        this.type = tpe;
        this.ami = image;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Environment acquire() {
        final AmazonEC2 aws = this.client.get();
        try {
            final RunInstancesResult result = aws.runInstances(
                new RunInstancesRequest()
                    .withInstanceType(this.type)
                    .withImageId(this.ami)
                    .withMinCount(1)
                    .withMaxCount(1)
            );
            final List<Instance> instances =
                result.getReservation().getInstances();
            if (instances.isEmpty()) {
                throw new IllegalStateException(
                    "failed top run an EC2 instance"
                );
            }
            final Instance instance = instances.get(0);
            Logger.info(
                this,
                "instance %s/%s created",
                instance.getInstanceId(),
                instance.getPublicIpAddress()
            );
            return new EC2Environment(
                this.whenReady(aws, instance.getInstanceId()),
                this.client
            );
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Return instanceId when ready.
     * @param aws EC2 gateway
     * @param name InstancID
     * @return The same ID
     */
    private String whenReady(final AmazonEC2 aws, final String name) {
        while (true) {
            final DescribeInstanceStatusResult result =
                aws.describeInstanceStatus(
                    new DescribeInstanceStatusRequest()
                        .withInstanceIds(name)
                        .withMaxResults(1)
                );
            final InstanceStatus status = result.getInstanceStatuses().get(0);
            final InstanceState state = status.getInstanceState();
            Logger.info(
                this,
                "instance %s/%s is in '%s' state (code=%d)",
                status.getInstanceId(),
                status.getAvailabilityZone(),
                state.getName(),
                state.getCode()
            );
            if ("running".equals(state.getName())) {
                break;
            }
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
        return name;
    }

}
