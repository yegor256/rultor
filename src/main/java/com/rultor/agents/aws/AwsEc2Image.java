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

import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import lombok.ToString;

/**
 * Amazon EC2 instance image to run.
 *
 * @since 1.77
 */
@Immutable
@ToString
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
public final class AwsEc2Image {
    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Amazon machine image id.
     */
    private final String image;

    /**
     * AWS Instance type.
     */
    private final transient InstanceType type;

    /**
     * EC2 security group.
     */
    private final String sgroup;

    /**
     * Ctor.
     * @param api AwsEc2 api client
     * @param image Ec2 instance ami_id
     * @param type Instance type
     * @param grp Security group
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public AwsEc2Image(final AwsEc2 api, final String image,
        final String type, final String grp) {
        this.api = api;
        if (image.isEmpty()) {
            throw new IllegalArgumentException(
                "Machine image id is mandatory"
            );
        }
        this.image = image;
        if (type.isEmpty()) {
            throw new IllegalArgumentException(
                "Machine type is mandatory"
            );
        }
        this.type = InstanceType.fromValue(type);
        if (grp.isEmpty()) {
            throw new IllegalArgumentException(
                "Security group is mandatory"
            );
        }
        this.sgroup = grp;
    }

    /**
     * Run image.
     * @return Instance_id
     */
    public AwsEc2Instance run() {
        final RunInstancesRequest request = new RunInstancesRequest()
            .withSecurityGroups(this.sgroup)
            .withImageId(this.image)
            .withInstanceType(this.type)
            .withMaxCount(1)
            .withMinCount(1);
        Logger.info(
            this, "Starting a new AWS instance, image=%s, type=%s, group=%s ...",
            this.image, this.type, this.sgroup
        );
        final RunInstancesResult response =
            this.api.aws().runInstances(request);
        final String iid = response.getReservation().getInstances().get(0).getInstanceId();
        Logger.info(this, "AWS instance %s launched", iid);
        return new AwsEc2Instance(this.api, iid);
    }
}
