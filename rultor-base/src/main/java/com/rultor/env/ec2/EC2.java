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
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.env.Environment;
import com.rultor.env.Environments;
import java.io.IOException;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon EC2 environments.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "type", "ami", "group", "client" })
@Loggable(Loggable.DEBUG)
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
     * EC2 security group.
     */
    private final transient String group;

    /**
     * EC2 key pair.
     */
    private final transient String pair;

    /**
     * EC2 client.
     */
    private final transient EC2Client client;

    /**
     * Public ctor.
     * @param tpe Instance type, for example "t1.micro"
     * @param image AMI name
     * @param grp Security group
     * @param par Key pair
     * @param akey AWS key
     * @param scrt AWS secret
     * @checkstyle ParameterNumber (5 lines)
     */
    public EC2(final String tpe, final String image, final String grp,
        final String par, final String akey, final String scrt) {
        this(tpe, image, grp, par, new EC2Client.Simple(akey, scrt));
    }

    /**
     * Public ctor.
     * @param tpe Instance type, for example "t1.micro"
     * @param image AMI name
     * @param grp Security group
     * @param par Key pair
     * @param clnt EC2 client
     * @checkstyle ParameterNumber (5 lines)
     */
    public EC2(final String tpe, final String image, final String grp,
        final String par, final EC2Client clnt) {
        this.type = tpe;
        this.ami = image;
        this.group = grp;
        this.pair = par;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Environment acquire() throws IOException {
        final AmazonEC2 aws = this.client.get();
        try {
            final RunInstancesResult result = aws.runInstances(
                new RunInstancesRequest()
                    .withInstanceType(this.type)
                    .withImageId(this.ami)
                    .withSecurityGroups(this.group)
                    .withKeyName(this.pair)
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
                "instance %s created, key=%s, platform=%s",
                instance.getInstanceId(),
                instance.getKeyName(),
                instance.getPlatform()
            );
            return new EC2Environment(instance.getInstanceId(), this.client);
        } finally {
            aws.shutdown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String face() {
        return Logger.format(
            "EC2 %s instance with '%s' AMI",
            this.type,
            this.ami
        );
    }

}
