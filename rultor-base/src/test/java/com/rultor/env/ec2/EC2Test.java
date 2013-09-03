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
     * Acquire Test.
     *
     * @throws IOException If some problem inside.
     */
    @Test
    public void acquire() throws IOException {
        final EC2 ectwo = this.prepareTestData();
        final Environment environment = ectwo.acquire();
        MatcherAssert.assertThat(
            environment,
            org.hamcrest.Matchers.notNullValue()
        );
    }
    /**
     * Prepare test data.
     *
     * @return EC2.
     */
    @SuppressWarnings("unchecked")
    private EC2 prepareTestData() {
        final Work work = Mockito.mock(Work.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final EC2Client client = Mockito.mock(EC2Client.class);
        final AmazonEC2 aws = Mockito.mock(AmazonEC2.class);
        final RunInstancesResult result =
            Mockito.mock(RunInstancesResult.class);
        final Reservation reservation = Mockito.mock(Reservation.class);
        final Instance instance = Mockito.mock(Instance.class);
        final List<Instance> instances =
            (List<Instance>) Mockito.mock(List.class);
        Mockito.when(
            client.get()
        ).thenReturn(aws);
        Mockito.when(
            aws.runInstances(
                Matchers.any(RunInstancesRequest.class)
            )
        ).thenReturn(result);
        Mockito.when(
            result.getReservation()
        ).thenReturn(reservation);
        Mockito.when(reservation.getInstances())
            .thenReturn(instances);
        Mockito.when(
            instances.isEmpty()
        ).thenReturn(false);
        Mockito.when(
            instances.get(0)
        ).thenReturn(instance);
        Mockito.when(
            instance.getInstanceId()
        ).thenReturn("InstanceId");
        Mockito.when(
            work.unit()
        ).thenReturn("Unit");
        Mockito.when(
            work.owner()
        ).thenReturn(new URN());
        Mockito.when(
            work.scheduled()
        ).thenReturn(new Time());
        final EC2 ectwo =
            new EC2(work, wallet, "type", "image", "group", "par", client);
        return ectwo;
    }
}
