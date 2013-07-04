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
package com.rultor.conveyer;

import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.manifests.Manifests;
import com.rexsl.test.RestTester;
import com.rultor.aws.SQSClient;
import com.rultor.queue.SQSQueue;
import com.rultor.repo.ClasspathRepo;
import com.rultor.users.AwsUsers;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Main entry point to the JAR.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode
@Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
public final class Main {

    /**
     * It's a utility class.
     */
    private Main() {
        // intentionally empty
    }

    /**
     * Entrance.
     * @param args Optional arguments
     * @throws Exception If something is wrong
     */
    public static void main(final String[] args) throws Exception {
        final OptionParser parser = new OptionParser();
        parser.accepts("dynamo-key", "DynamoDB key").withRequiredArg();
        parser.accepts("dynamo-secret", "DynamoDB secret").withRequiredArg();
        parser.accepts("dynamo-prefix", "DynamoDB prefix").withRequiredArg();
        parser.accepts("sqs-key", "SQS key").withRequiredArg();
        parser.accepts("sqs-secret", "SQS secret").withRequiredArg();
        parser.accepts("sqs-url", "SQS URL").withRequiredArg();
        final OptionSet options = parser.parse(args);
        final SimpleConveyer conveyer = new SimpleConveyer(
            new SQSQueue(
                new SQSClient.Simple(
                    options.valueOf("sqs-key").toString(),
                    options.valueOf("sqs-secret").toString(),
                    options.valueOf("sqs-url").toString()
                )
            ),
            new ClasspathRepo(),
            new AwsUsers(
                new Region.Prefixed(
                    new Region.Simple(
                        new Credentials.Simple(
                            options.valueOf("dynamo-key").toString(),
                            options.valueOf("dynamo-secret").toString()
                        )
                    ),
                    options.valueOf("dynamo-prefix").toString()
                )
            )
        );
        conveyer.start();
        while (true) {
            if (!Manifests.read("Rultor-Revision").equals(Main.revision())) {
                break;
            }
            TimeUnit.MINUTES.sleep(1);
        }
        conveyer.close();
    }

    /**
     * Get revision from the web server.
     * @return Revision found there
     * @throws Exception If something is wrong
     */
    private static String revision() throws Exception {
        return RestTester.start(new URI("http://www.rultor.com/misc/version"))
            .get("read revision from web node")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .getBody();
    }

}
