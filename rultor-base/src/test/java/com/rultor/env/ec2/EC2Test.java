/**
 * Copyright (c) 2009-2014, rultor.com
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
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.jcabi.urn.URN;
import com.rultor.aws.EC2Client;
import com.rultor.env.Environment;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Wallet;
import com.rultor.tools.Time;
import java.io.IOException;
import java.net.URL;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link EC2}.
 *
 * @author Vaibhav Paliwal (vaibhavpaliwal99@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class EC2Test {

    /**
     * Assume we're online.
     */
    @Before
    public void weAreOnline() {
        try {
            new URL("http://www.google.com").getContent();
        } catch (final IOException ex) {
            Assume.assumeTrue(false);
        }
    }

    /**
     * Acquire Environment from EC2.
     * @throws IOException If some problem inside.
     */
    @Test
    public void acquireEnvironment() throws IOException {
        final EC2 envs = this.mockEnvironment();
        final Environment environment = envs.acquire();
        MatcherAssert.assertThat(environment, Matchers.notNullValue());
    }

    /**
     * Mock the Environment.
     * @return EC2.
     */
    private EC2 mockEnvironment() {
        final Coordinates work = Mockito.mock(Coordinates.class);
        final Wallet wallet = Mockito.mock(Wallet.class);
        final EC2Client client = Mockito.mock(EC2Client.class);
        final AmazonEC2 aws = Mockito.mock(AmazonEC2.class);
        Mockito.when(client.get()).thenReturn(aws);
        Mockito.doReturn(
            new DescribeImagesResult().withImages(
                new Image().withBlockDeviceMappings(
                    new BlockDeviceMapping()
                        .withDeviceName("/dev/sda1")
                        .withEbs(new EbsBlockDevice())
                )
            )
        ).when(aws).describeImages(Mockito.any(DescribeImagesRequest.class));
        Mockito.doReturn(
            new RunInstancesResult().withReservation(
                new Reservation().withInstances(
                    new Instance()
                        .withImageId("ami-7676767")
                        .withInstanceId("i-909090")
                        .withInstanceType("m1.small")
                        .withKernelId("aki-89797978")
                        .withKeyName("my-key")
                        .withPublicIpAddress("192-168-0-1")
                        .withPlacement(
                            new Placement().withAvailabilityZone("eu-west-1")
                        )
                )
            )
        ).when(aws).runInstances(Mockito.any(RunInstancesRequest.class));
        Mockito.when(work.owner()).thenReturn(new URN());
        Mockito.when(work.scheduled()).thenReturn(new Time());
        final EC2 envs = new EC2(
            work, wallet, "type", "ami-ef9f2f1e", "group", "par",
            "eu-west-1a", client
        );
        return envs;
    }
}
