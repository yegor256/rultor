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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import java.security.SecureRandom;
import java.util.Random;
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
@Immutable
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
            final SimpleConveyer conveyer =
                new ConveyerBuilder(options).build();
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

}
