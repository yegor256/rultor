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
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.jcabi.aspects.Immutable;
import com.rultor.spi.Profile;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.yaml.snakeyaml.Yaml;

/**
 * Amazon via Amazon EC2 API.
 * @author Yuriy Alevohin (alevohin@mail.ru)
 * @version $Id$
 * @since 2.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile" })
public final class AmazonAPI implements Amazon {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param prof Profile
     */
    public AmazonAPI(final Profile prof) {
        this.profile = prof;
    }

    /**
     * Run EC2 instance.
     * @return EC2 Instance
     * @throws IOException if fails
     * @todo #629 Add waiting and check for success start EC2 instance
     */
    public Instance runOnDemand() throws IOException {
        final RunInstancesRequest request = this.request();
        final AmazonEC2 client = this.client();
        final RunInstancesResult result = client.runInstances(request);
        return result.getReservation().getInstances().get(0);
    }

    /**
     * Create on-demand instance request.
     * @return RunInstancesRequest
     * @throws IOException if fails
     * @todo #629 add check (check for null, check for type)
     */
    private RunInstancesRequest request() throws IOException {
        final RunInstancesRequest request = new RunInstancesRequest();
        final Profile.Defaults prof = new Profile.Defaults(this.profile);
        final String key = prof.text(
            "/p/entry[@key='ec2']/entry[@key='key']",
            null
        );
        request.withKeyName(key);
        final String type = prof.text(
            "/p/entry[@key='ec2']/entry[@key='type']",
            null
        );
        request.withInstanceType(type);
        request.withMinCount(1);
        request.withMaxCount(1);
        return request;
    }
    /**
     * Create Amazon EC2 client.
     * @return AmazonEC2
     * @throws IOException If fails
     */
    private AmazonEC2 client() throws IOException {
        final AmazonEC2 client = new AmazonEC2Client(
            this.credentials()
        );
        final Profile.Defaults prof = new Profile.Defaults(
            this.profile
        );
        final String region = prof.text(
            "/p/entry[@key='ec2']/entry[@key='zone']",
            Regions.DEFAULT_REGION.toString()
        ).replaceAll("-", "_").toUpperCase();
        client.setRegion(
            Region.getRegion(
                Regions.valueOf(region)
            )
        );
        return client;
    }

    /**
     * Create credentials.
     * @return AWSCredentials
     * @throws IOException if credential's file doesn't exists
     * @todo #629 add check for credentials (check for null,
     *  more verbose exception)
     */
    @SuppressWarnings("unchecked")
    private AWSCredentials credentials() throws IOException {
        final String config = IOUtils.toString(
            new FileInputStream(
                new Profile.Defaults(this.profile).text(
                    "/p/entry[@key='ec2']/entry[@key='credentials']",
                    "access/ec2.yml"
                )
            )
            , CharEncoding.UTF_8
        );
        final Yaml parser = new Yaml();
        final Object object = parser.load(config);
        if (object instanceof Map) {
            return new BasicAWSCredentials(
                ((Map<String, Object>) object).get("key").toString(),
                ((Map<String, Object>) object).get("secret").toString()
            );
        } else {
            throw new IllegalStateException(
                "Wrong ec2 credentials file"
            );
        }
    }
}
