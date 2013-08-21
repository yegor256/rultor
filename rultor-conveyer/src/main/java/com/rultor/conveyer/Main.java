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

import com.amazonaws.services.sqs.AmazonSQS;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.conveyer.audit.AuditUsers;
import com.rultor.queue.SQSQueue;
import com.rultor.repo.ClasspathRepo;
import com.rultor.spi.Queue;
import com.rultor.spi.Spec;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import com.rultor.users.AwsUsers;
import com.rultor.users.pgsql.PgClient;
import com.rultor.users.pgsql.PgUsers;
import java.io.File;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;

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
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.AvoidDuplicateLiterals" })
@Loggable(Loggable.DEBUG)
public final class Main {

    /**
     * Random.
     */
    private static final Random RND = new SecureRandom();

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
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public static void main(final String[] args) throws Exception {
        final OptionParser parser = Main.parser();
        final OptionSet options = parser.parse(args);
        if (options.has("help")) {
            parser.printHelpOn(Logger.stream(Level.INFO, Main.class));
        } else {
            final SimpleConveyer conveyer = Main.conveyer(options);
            Logger.info(Main.class, "Starting %s", conveyer);
            final long start = System.currentTimeMillis();
            conveyer.start();
            while (Main.alive(start, options)) {
                TimeUnit.SECONDS.sleep(1);
            }
            conveyer.close();
        }
    }

    /**
     * Are we still need to be alive?
     * @param start When scheduled
     * @param options Command line options
     * @return TRUE if we should still be alive
     * @throws Exception If fails
     */
    private static boolean alive(final long start, final OptionSet options)
        throws Exception {
        final boolean alive;
        if (options.has("lifetime")) {
            final long lifetime = Long.parseLong(
                options.valueOf("lifetime").toString()
            );
            alive = System.currentTimeMillis() - start < lifetime;
        } else {
            if (new Version().same()) {
                TimeUnit.SECONDS.sleep(Main.RND.nextInt(Tv.HUNDRED));
                alive = true;
            } else {
                alive = false;
            }
        }
        return alive;
    }

    /**
     * Build a parser.
     * @return Parser
     */
    private static OptionParser parser() {
        final OptionParser parser = new OptionParser();
        parser.accepts("help", "Show detailed instructions").forHelp();
        parser.accepts("threads", "In how many threads to run")
            .withRequiredArg().defaultsTo("5").ofType(String.class);
        parser.accepts("spec", "Text file with work specification")
            .withRequiredArg().ofType(String.class);
        parser.accepts("lifetime", "Maximum lifetime of the daemon, in millis")
            .withRequiredArg().ofType(String.class)
            .defaultsTo(Long.toString(TimeUnit.DAYS.toMillis(Tv.FIVE)));
        parser.accepts("dynamo-key", "Amazon DynamoDB access key")
            .withRequiredArg().ofType(String.class);
        parser.accepts("dynamo-secret", "Amazon DynamoDB secret key")
            .withRequiredArg().ofType(String.class);
        parser.accepts("dynamo-prefix", "Amazon DynamoDB table name prefix")
            .withRequiredArg().ofType(String.class);
        parser.accepts("sqs-key", "Amazon SQS access key")
            .withRequiredArg().ofType(String.class);
        parser.accepts("sqs-secret", "Amazon SQS secret key")
            .withRequiredArg().ofType(String.class);
        parser.accepts("sqs-url", "Amazon SQS URL")
            .withRequiredArg().ofType(String.class);
        parser.accepts("sqs-wallet-url", "Amazon SQS URL for wallets")
            .withRequiredArg().ofType(String.class);
        parser.accepts("pgsql-url", "PostgreSQL JDBC URL")
            .withRequiredArg().ofType(String.class);
        parser.accepts("pgsql-password", "PostgreSQL password")
            .withRequiredArg().ofType(String.class);
        return parser;
    }

    /**
     * Create conveyer as requested in the options.
     * @param options Options
     * @return Conveyer
     * @throws Exception If fails
     */
    @Loggable(Loggable.INFO)
    private static SimpleConveyer conveyer(final OptionSet options)
        throws Exception {
        final Queue queue;
        Users users;
        if (options.has("spec")) {
            final Work work = new Work.Simple(
                URN.create("urn:facebook:1"),
                "default"
            );
            queue = new Queue() {
                private final transient AtomicBoolean done =
                    new AtomicBoolean();
                @Override
                public void push(final Work work) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Work pull(final int limit, final TimeUnit unit) {
                    final Work pulled;
                    if (done.compareAndSet(false, true)) {
                        pulled = work;
                        done.set(true);
                    } else {
                        pulled = new Work.None();
                    }
                    return pulled;
                }
            };
            users = new FakeUsers(
                work,
                new Spec.Simple(
                    FileUtils.readFileToString(
                        new File(options.valueOf("spec").toString()),
                        CharEncoding.UTF_8
                    )
                )
            );
        } else {
            final String sqs = options.valueOf("sqs-url").toString();
            if (options.has("sqs-key")) {
                queue = new SQSQueue(
                    new SQSClient.Simple(
                        options.valueOf("sqs-key").toString(),
                        options.valueOf("sqs-secret").toString(),
                        sqs
                    )
                );
            } else {
                queue = new SQSQueue(new SQSClient.Assumed(sqs));
            }
            if (options.has("dynamo-key")) {
                users = new AwsUsers(
                    new Region.Prefixed(
                        new Region.Simple(
                            new Credentials.Simple(
                                options.valueOf("dynamo-key").toString(),
                                options.valueOf("dynamo-secret").toString()
                            )
                        ),
                        options.valueOf("dynamo-prefix").toString()
                    ),
                    new SQSClient.Simple(
                        options.valueOf("sqs-key").toString(),
                        options.valueOf("sqs-secret").toString(),
                        options.valueOf("sqs-wallet-url").toString()
                    )
                );
            } else {
                users = new AwsUsers(
                    new Region.Prefixed(
                        new Region.Simple(new Credentials.Assumed()),
                        options.valueOf("dynamo-prefix").toString()
                    ),
                    new SQSClient.Assumed(
                        options.valueOf("sqs-wallet-url").toString()
                    )
                );
            }
            users = new PgUsers(
                new PgClient.Simple(
                    options.valueOf("pgsql-url").toString(),
                    options.valueOf("pgsql-password").toString()
                ),
                new SQSClient() {
                    @Override
                    public AmazonSQS get() {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public String url() {
                        throw new UnsupportedOperationException();
                    }
                },
                users
            );

        }
        return new SimpleConveyer(
            queue, new ClasspathRepo(), new AuditUsers(users),
            Integer.parseInt(options.valueOf("threads").toString())
        );
    }

}
