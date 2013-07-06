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
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.rexsl.test.RestTester;
import com.rultor.aws.SQSClient;
import com.rultor.base.Empty;
import com.rultor.drain.Trash;
import com.rultor.queue.SQSQueue;
import com.rultor.repo.ClasspathRepo;
import com.rultor.users.AwsUsers;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@ToString
@EqualsAndHashCode
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
    @Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
    public static void main(final String[] args) throws Exception {
        final OptionParser parser = Main.parser();
        final OptionSet options = parser.parse(args);
        if (options.has("help")) {
            parser.printHelpOn(Logger.stream(Level.INFO, Main.class));
        } else {
            final SimpleConveyer conveyer = Main.conveyer(options);
            conveyer.start();
            final String mine = Manifests.read("Rultor-Revision");
            final long start = System.currentTimeMillis();
            while (true) {
                final String base = Main.revision();
                if (!mine.equals(base)) {
                    Logger.info(
                        Main.class,
                        "#main(): we're in %s while %s is the newest one",
                        mine,
                        base
                    );
                    break;
                }
                TimeUnit.MINUTES.sleep(1);
                Logger.info(
                    Main.class,
                    "#main(): still alive in %s (%[ms]s already)...",
                    mine,
                    System.currentTimeMillis() - start
                );
            }
            conveyer.close();
        }
    }

    /**
     * Build a parser.
     * @return Parser
     */
    private static OptionParser parser() {
        final OptionParser parser = new OptionParser();
        parser.accepts("help", "detailed instructions").forHelp();
        parser.accepts("dynamo-key", "DynamoDB key")
            .withRequiredArg().ofType(String.class).required();
        parser.accepts("dynamo-secret", "DynamoDB secret")
            .withRequiredArg().ofType(String.class).required();
        parser.accepts("dynamo-prefix", "DynamoDB prefix")
            .withRequiredArg().ofType(String.class).required();
        parser.accepts("sqs-key", "SQS key")
            .withRequiredArg().ofType(String.class).required();
        parser.accepts("sqs-secret", "SQS secret")
            .withRequiredArg().ofType(String.class).required();
        parser.accepts("sqs-url", "SQS URL")
            .withRequiredArg().ofType(String.class).required();
        return parser;
    }

    /**
     * Create conveyer as requested in the options.
     * @param options Options
     * @return Conveyer
     */
    @Loggable(Loggable.INFO)
    private static SimpleConveyer conveyer(final OptionSet options) {
        assert new Empty().toString() != null;
        assert new Trash().toString() != null;
        return new SimpleConveyer(
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
