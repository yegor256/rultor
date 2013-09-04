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
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.rultor.aws.EC2Client;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link EC2Environment}.
 *
 * @author Vaibhav Paliwal (vaibhavpaliwal99@gmail.com)
 * @version $Id$
 */
public final class EC2EnvironmentTest {

    /**
     * Create InetAddress from EC2Environment.
     *
     * @throws IOException If some problem inside.
     */
    @Test
    public void createInetAddress() throws IOException {
        final EC2Environment env = this.mockEnvironment();
        final InetAddress address = env.address();
        MatcherAssert.assertThat(
            address,
            Matchers.notNullValue()
        );
    }

    /**
     * Close EC2Environment.
     *
     * @throws IOException If some problem inside.
     */
    @Test
    public void closeEnvironment() throws IOException {
        final EC2Environment env = this.mockEnvironment();
        env.close();
    }
    /**
     * Implementaion EC2Environment's toString() method.
     */
    public void toStringImplementaion() {
        final EC2Environment env = this.mockEnvironment();
        MatcherAssert.assertThat(
            env.toString(),
            Matchers
            .containsString(
                "EC2 `instance` instance accessed with Mock for EC2Client"
            )
        );
    }

    /**
     * Mock the Environment.
     * @return EC2Environment
     * @checkstyle ExecutableStatementCount (50 lines)
     */
    private EC2Environment mockEnvironment() {
        final Work work = Mockito.mock(Work.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final EC2Client client = Mockito.mock(EC2Client.class);
        final AmazonEC2 aws = Mockito.mock(AmazonEC2.class);
        final DescribeInstancesResult instanceresult =
            Mockito.mock(DescribeInstancesResult.class);
        final Reservation reservation = Mockito.mock(Reservation.class);
        final List<Reservation> reservations = Arrays.asList(reservation);
        final Instance instance = Mockito.mock(Instance.class);
        final List<Instance> instances =
            Arrays.asList(instance);
        final Placement placement = Mockito.mock(Placement.class);
        final InstanceState instanceState = Mockito.mock(InstanceState.class);
        final TerminateInstancesResult result =
            Mockito.mock(TerminateInstancesResult.class);
        final InstanceStateChange stateChange =
            Mockito.mock(InstanceStateChange.class);
        final List<InstanceStateChange> stateChanges =
            Arrays.asList(stateChange);
        Mockito.when(client.get()).thenReturn(aws);
        Mockito
            .when(
                aws.describeInstances(
                    Mockito.any(DescribeInstancesRequest.class)
            )
        ).thenReturn(instanceresult);
        Mockito.when(
            instanceresult.getReservations()
        ).thenReturn(reservations);
        Mockito.when(reservation.getInstances()).thenReturn(instances);
        Mockito.when(instance.getPlacement()).thenReturn(placement);
        Mockito.when(instance.getState()).thenReturn(instanceState);
        Mockito.when(instanceState.getName()).thenReturn("running");
        Mockito.when(
            aws.terminateInstances(
                Mockito.any(TerminateInstancesRequest.class)
            )
        ).thenReturn(result);
        Mockito.when(result.getTerminatingInstances()).thenReturn(stateChanges);
        Mockito.when(instance.getLaunchTime()).thenReturn(new Date());
        Mockito.when(instance.getInstanceType()).thenReturn("InstanceType");
        Mockito.when(placement.getAvailabilityZone()).thenReturn("ZONE");
        final EC2Environment env =
            new EC2Environment(work, wallet, "instance", client);
        return env;
    }
}
