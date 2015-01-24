/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.agents.ec2;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;

/**
 * Amazon EC2 instance on demand.
 *
 * @author Marton Horvath (marton.horvath@m323.org)
 * @version $Id$
 * @since 2.0
 */
@Immutable
public class AmazonEC2 implements Amazon {
    /**
     * AWS credentials.
     */
    private final transient AWSCredentials credentials;
    /**
     * AWS region.
     */
    private final transient String region;
    /**
     * EC2 instance type.
     */
    private final transient String itype;
    /**
     * EC2 instance key pair name.
     */
    private final transient String keyname;

    /**
     * Public ctor.
     * @param cred Credentials
     * @param zone AWS region
     * @param type Instance type
     * @param key Instance key pair name
     * @checkstyle ParameterNumberCheck (12 lines)
     */
    public AmazonEC2(
        @NotNull final AWSCredentials cred,
        @NotNull final String zone,
        @NotNull final String type,
        @NotNull final String key) {
        super();
        this.credentials = cred;
        this.region = zone;
        this.itype = type;
        this.keyname = key;
    }

    // @checkstyle MagicNumberCheck (30 lines)
    @Override
    public final Instance runOnDemand() throws IOException {
        Instance instance = null;
        final AmazonEC2Client client = new AmazonEC2Client(
            this.credentials
        );
        client.setEndpoint(this.region);
        final RunInstancesRequest request = this.createRequest();
        final List<Instance> instances = client.runInstances(
            request
        ).getReservation().getInstances();
        if ((instances != null) && !instances.isEmpty()) {
            instance = instances.get(0);
        }
        InstanceState state = instance.getState();
        if (state.getCode() < 16) {
            final DescribeInstanceStatusRequest statusreq =
                new DescribeInstanceStatusRequest();
            statusreq.withInstanceIds(instance.getInstanceId());
            while (state.getCode() < 16) {
                try {
                    TimeUnit.SECONDS.sleep(Tv.FIFTEEN);
                } catch (final InterruptedException ex) {
                    continue;
                }
                final DescribeInstanceStatusResult instanceStatus = client
                    .describeInstanceStatus(statusreq);
                state = instanceStatus.getInstanceStatuses().get(0)
                    .getInstanceState();
            }
        }
        return instance;
    }

    /**
     * Creates a new {@link RunInstancesRequest}.
     * @return The {@link RunInstancesRequest} created
     */
    private RunInstancesRequest createRequest() {
        final RunInstancesRequest request = new RunInstancesRequest();
        request.withKeyName(this.keyname);
        request.withMinCount(1);
        request.withMaxCount(1);
        request.setInstanceType(this.itype);
        return request;
    }
}
