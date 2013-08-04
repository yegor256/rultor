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
package com.rultor.life;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.manifests.Manifests;
import com.rultor.aws.SQSClient;
import com.rultor.queue.SQSQueue;
import com.rultor.repo.ClasspathRepo;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Stand;
import com.rultor.spi.Users;
import com.rultor.users.AwsUsers;
import com.rultor.users.mongo.Mongo;
import com.rultor.users.mongo.MongoUsers;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Production profile.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@ToString
@EqualsAndHashCode
@Loggable(Loggable.INFO)
final class Production implements Profile {

    /**
     * Quartz to use.
     */
    private final transient SQSQuartz quartz = new SQSQuartz(
        this.users(),
        this.queue(),
        new SQSClient.Simple(
            Manifests.read("Rultor-SQSKey"),
            Manifests.read("Rultor-SQSSecret"),
            Manifests.read("Rultor-SQSQuartz")
        )
    );

    /**
     * SQS pulse sensor.
     */
    private final transient SQSPulseSensor sensor = new SQSPulseSensor(
        this.users(),
        this.repo(),
        new SQSClient.Simple(
            Manifests.read("Rultor-SQSKey"),
            Manifests.read("Rultor-SQSSecret"),
            Stand.QUEUE.toString()
        )
    );

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(forever = true)
    public Repo repo() {
        return new ClasspathRepo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(forever = true)
    public Users users() {
        return new MongoUsers(
            new Mongo.Simple(
                Manifests.read("Rultor-MongoHost"),
                Integer.parseInt(Manifests.read("Rultor-MongoPort")),
                Manifests.read("Rultor-MongoName"),
                Manifests.read("Rultor-MongoUser"),
                Manifests.read("Rultor-MongoPassword")
            ),
            new AwsUsers(
                new Region.Prefixed(
                    new Region.Simple(
                        new Credentials.Simple(
                            Manifests.read("Rultor-DynamoKey"),
                            Manifests.read("Rultor-DynamoSecret")
                        )
                    ),
                    Manifests.read("Rultor-DynamoPrefix")
                )
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(forever = true)
    public Queue queue() {
        return new SQSQueue(
            new SQSClient.Simple(
                Manifests.read("Rultor-SQSKey"),
                Manifests.read("Rultor-SQSSecret"),
                Manifests.read("Rultor-SQSUrl")
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.quartz.close();
        this.sensor.close();
    }

}
