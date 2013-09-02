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
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.jcabi.urn.URN;
import com.rultor.aws.EC2Client;
import com.rultor.env.Environment;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Test case for {@link EC2}.
 *
 * @author Vaibhav Paliwal (vaibhavpaliwal99@gmail.com)
 * @version $Id$
 */
public final class EC2Test {
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
     * RunInstancesResult.
     */
    private transient RunInstancesResult result;
    /**
     * Reservation.
     */
    private transient Reservation reservation;
    /**
     * Instance.
     */
    private transient Instance instance;
    /**
     * Instances.
     */
    private transient List<Instance> instances;

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
        this.result = Mockito.mock(RunInstancesResult.class);
        this.reservation = Mockito.mock(Reservation.class);
        this.instance = Mockito.mock(Instance.class);
        this.instances = (List<Instance>) Mockito.mock(List.class);
        this.mockMethods();
    }
    /**
     * Mock methods.
     */
    private void mockMethods() {
        Mockito.when(
            this.client.get()
        ).thenReturn(this.aws);
        Mockito.when(
            this.aws.runInstances(
                Matchers.any(RunInstancesRequest.class)
            )
        ).thenReturn(this.result);
        Mockito.when(
            this.result.getReservation()
        ).thenReturn(this.reservation);
        Mockito.when(this.reservation.getInstances())
            .thenReturn(this.instances);
        Mockito.when(
            this.instances.isEmpty()
        ).thenReturn(false);
        Mockito.when(
            this.instances.get(0)
        ).thenReturn(this.instance);
        Mockito.when(
            this.instance.getInstanceId()
        ).thenReturn("InstanceId");
        Mockito.when(
            this.work.unit()
        ).thenReturn("Unit");
        Mockito.when(
            this.work.owner()
        ).thenReturn(new URN());
        Mockito.when(
            this.work.scheduled()
        ).thenReturn(new Time());
    }

    /**
     * Acquire Test.
     *
     * @throws IOException If some problem inside.
     */
    @Test
    public void acquire() throws IOException {
        final EC2 ectwo = new EC2(this.work, this.wallet, "type", "image",
            "group", "par", this.client);
        final Environment environment = ectwo.acquire();
        MatcherAssert.assertThat(
            environment,
            org.hamcrest.Matchers.notNullValue()
        );
    }
}
