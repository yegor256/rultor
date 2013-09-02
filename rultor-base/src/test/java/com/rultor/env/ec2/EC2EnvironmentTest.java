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
import java.util.Date;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Test case for {@link EC2Environment}.
 *
 * @author Vaibhav Paliwal (vaibhavpaliwal99@gmail.com)
 * @version $Id$
 */
public final class EC2EnvironmentTest {
    /**
     * Work.
     */
    private transient Work work;
    /**
     * Wallet.
     */
    private transient Wallet wallet;
    /**
     * Client.
     */
    private transient EC2Client client;
    /**
     * AmazonEC2.
     */
    private transient AmazonEC2 aws;
    /**
     * DescribeInstancesResult.
     */
    private transient DescribeInstancesResult instanceresult;
    /**
     * Reservations.
     */
    private transient List<Reservation> reservations;
    /**
     * Reservation.
     */
    private transient Reservation reservation;
    /**
     * Instances.
     */
    private transient List<Instance> instances;
    /**
     * Instance.
     */
    private transient Instance instance;
    /**
     * Placement.
     */
    private transient Placement placement;
    /**
     * InstanceState.
     */
    private transient InstanceState instanceState;
    /**
     * Result.
     */
    private transient TerminateInstancesResult result;
    /**
     * InstanceStateChange.
     */
    private transient InstanceStateChange stateChange;
    /**
     * InstanceStateChanges.
     */
    private transient List<InstanceStateChange> stateChanges;

    /**
     * Mock The objects.
     */
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.work = Mockito.mock(Work.class);
        this.wallet = Mockito.mock(Wallet.class);
        this.client = Mockito.mock(EC2Client.class);
        this.aws = Mockito.mock(AmazonEC2.class);
        this.instanceresult = Mockito
            .mock(DescribeInstancesResult.class);
        this.reservations = (List<Reservation>) Mockito.mock(List.class);
        this.reservation = Mockito.mock(Reservation.class);
        this.instances = (List<Instance>) Mockito.mock(List.class);
        this.instance = Mockito.mock(Instance.class);
        this.placement = Mockito.mock(Placement.class);
        this.instanceState = Mockito.mock(InstanceState.class);
        this.result = Mockito.mock(TerminateInstancesResult.class);
        this.stateChange = Mockito.mock(InstanceStateChange.class);
        this.stateChanges = (List<InstanceStateChange>) Mockito
            .mock(List.class);
        this.mockMethods();
    }
    /**
     * Mock methods.
     */
    private void mockMethods() {
        Mockito.when(this.client.get()).thenReturn(this.aws);
        Mockito.when(
            this.aws.describeInstances(
                Matchers.any(
                    DescribeInstancesRequest.class
                )
            )
        ).thenReturn(this.instanceresult);
        Mockito.when(
            this.instanceresult.getReservations()
        ).thenReturn(this.reservations);
        Mockito.when(
            this.reservations.get(0)
        ).thenReturn(this.reservation);
        Mockito.when(
            this.reservation.getInstances()
        ).thenReturn(this.instances);
        Mockito.when(
            this.instances.isEmpty()
        ).thenReturn(false);
        Mockito.when(
            this.instances.get(0)
        ).thenReturn(this.instance);
        Mockito.when(
            this.instance.getPlacement()
        ).thenReturn(this.placement);
        Mockito.when(
            this.instance.getState()
        ).thenReturn(this.instanceState);
        Mockito.when(
            this.instanceState.getName()
        ).thenReturn("running");
        Mockito.when(
            this.aws.terminateInstances(
                Matchers
                    .any(TerminateInstancesRequest.class)
            )
        ).thenReturn(this.result);
        Mockito.when(
            this.result.getTerminatingInstances()
        ).thenReturn(this.stateChanges);
        Mockito.when(
            this.stateChanges.get(0)
        ).thenReturn(this.stateChange);
        Mockito.when(
            this.instance.getLaunchTime()
        ).thenReturn(new Date());
        Mockito.when(
            this.instance.getInstanceType()
        ).thenReturn("InstanceType");
        Mockito.when(
            this.placement.getAvailabilityZone()
        ).thenReturn("ZONE");
    }

    /**
     * Creation of the InetAddress test.
     *
     * @throws IOException If some problem inside.
     */
    @Test
    public void address() throws IOException {
        final EC2Environment eC2Environment = new EC2Environment(this.work,
            this.wallet, "instance", this.client);
        final InetAddress inetAddress = eC2Environment.address();
        MatcherAssert.assertThat(
            inetAddress,
            org.hamcrest.Matchers.notNullValue()
        );
    }

    /**
     * Test method for close.
     *
     * @throws IOException If some problem inside.
     */
    @Test
    public void close() throws IOException {
        final EC2Environment eC2Environment = new EC2Environment(this.work,
            this.wallet, "instance2", this.client);
        eC2Environment.close();
    }
}
