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
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.EC2Client;
import com.rultor.env.Environment;
import com.rultor.snapshot.Step;
import com.rultor.spi.Work;
import com.rultor.tools.Dollars;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * Amazon EC2 environment.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "name", "client" })
@Loggable(Loggable.DEBUG)
final class EC2Environment implements Environment {

    /**
     * Approximate prices for certain instance types.
     */
    private static final ImmutableMap<String, Double> PRICES =
        new ImmutableMap.Builder<String, Double>()
            // @checkstyle MagicNumber (10 lines)
            .put(".+\\.micro", 0.02d)
            .put(".+\\.small", 0.06d)
            .put(".+\\.medium", 0.12d)
            .put(".+\\.large", 0.24d)
            .put(".+\\.xlarge", 0.48d)
            .put(".+\\.2xlarge", 1d)
            .put(".+\\.4xlarge", 1.64d)
            .put(".+\\.8xlarge", 3.5d)
            .build();

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Instance ID.
     */
    private final transient String name;

    /**
     * EC2 client.
     */
    private final transient EC2Client client;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param instance Instance ID
     * @param clnt EC2 client
     */
    protected EC2Environment(final Work wrk, final String instance,
        final EC2Client clnt) {
        this.work = wrk;
        this.name = instance;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "EC2 `%s` instance accessed with %s",
            this.name, this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress address() throws IOException {
        final Instance instance = this.instance();
        return InetAddress.getByAddress(
            instance.getPublicDnsName(),
            InetAddress.getByName(
                instance.getPublicIpAddress()
            ).getAddress()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Step("EC2 instance ${self.name} terminated")
    public void close() throws IOException {
        final AmazonEC2 aws = this.client.get();
        try {
            final Instance instance = aws.describeInstances(
                new DescribeInstancesRequest().withInstanceIds(this.name)
            ).getReservations().get(0).getInstances().get(0);
            final TerminateInstancesResult result = aws.terminateInstances(
                new TerminateInstancesRequest()
                    .withInstanceIds(this.name)
            );
            final InstanceStateChange change =
                result.getTerminatingInstances().get(0);
            final long age = System.currentTimeMillis()
                - instance.getLaunchTime().getTime();
            final Dollars cost = EC2Environment.costOf(
                instance.getInstanceType(),
                instance.getPlacement().getAvailabilityZone(),
                age
            );
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "EC2 instance `%s`/`%s` terminated, after %[ms]s of activity, approx. %s",
                change.getInstanceId(),
                instance.getInstanceType(),
                age,
                cost
            );
            this.work.charge(
                Logger.format(
                    "%[ms]s of AWS EC2 `%s` instance",
                    age,
                    instance.getInstanceType()
                ),
                cost
            );
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Return instance when it's ready.
     * @return Instance
     * @throws IOException If fails
     */
    @RetryOnFailure
    @Step("EC2 instance `${result.getInstanceType()}` is ready")
    private Instance instance() throws IOException {
        final AmazonEC2 aws = this.client.get();
        final DescribeInstancesRequest request = new DescribeInstancesRequest()
            .withInstanceIds(this.name);
        try {
            while (true) {
                final DescribeInstancesResult result =
                    aws.describeInstances(request);
                final Instance instance =
                    result.getReservations().get(0).getInstances().get(0);
                final InstanceState state = instance.getState();
                Logger.info(
                    this,
                    "instance `%s`/`%s` is in `%s` state (code=%d)",
                    instance.getInstanceId(),
                    instance.getPlacement().getAvailabilityZone(),
                    state.getName(),
                    state.getCode()
                );
                if ("running".equals(state.getName())) {
                    return instance;
                }
                if (!"pending".equals(state.getName())) {
                    throw new IllegalStateException(
                        String.format(
                            "instance `%s` is in invalid state `%s`",
                            instance.getInstanceId(),
                            state.getName()
                        )
                    );
                }
                try {
                    TimeUnit.SECONDS.sleep(Tv.FIFTEEN);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
            }
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Calculate cost of time spent, in points (one millionth of USD).
     * @param type Type of EC2 instance
     * @param zone Availability zone
     * @param msec Time spent
     * @return The price of the instance time
     * @throws IOException If IO problem inside
     */
    private static Dollars costOf(final String type, final String zone,
        final long msec) throws IOException {
        assert zone != null;
        final int hours = (int) (1 + msec / TimeUnit.HOURS.toMillis(1));
        Double hourly = 1d;
        for (Map.Entry<String, Double> ent : EC2Environment.PRICES.entrySet()) {
            if (type.matches(ent.getKey())) {
                hourly = ent.getValue();
                break;
            }
        }
        return new Dollars((long) (hours * hourly * Tv.MILLION));
    }

}
