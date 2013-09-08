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
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.conveyer.audit.AuditUsers;
import com.rultor.conveyer.fake.FakeUsers;
import com.rultor.queue.SQSQueue;
import com.rultor.repo.ClasspathRepo;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Queue;
import com.rultor.spi.Spec;
import com.rultor.users.AwsUsers;
import com.rultor.users.pgsql.PgClient;
import com.rultor.users.pgsql.PgUsers;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import joptsimple.OptionSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;

/**
 * Conveyer builder.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@Loggable(Loggable.INFO)
final class ConveyerBuilder {

    /**
     * Options to build from.
     */
    private final transient OptionSet options;

    /**
     * Ctor.
     * @param opts Options
     */
    protected ConveyerBuilder(final OptionSet opts) {
        this.options = opts;
    }

    /**
     * Create conveyer as requested in the this.options.
     * @return Conveyer
     * @throws Exception If fails
     */
    public SimpleConveyer build() throws Exception {
        final SimpleConveyer conveyer;
        if (this.options.has("spec")) {
            conveyer = this.local();
        } else {
            conveyer = this.standard();
        }
        return conveyer;
    }

    /**
     * Create standard conveyer.
     * @return Conveyer
     * @throws Exception If fails
     */
    public SimpleConveyer standard() throws Exception {
        final String sqs = this.options.valueOf("sqs-url").toString();
        final Queue queue;
        if (this.options.has("sqs-key")) {
            queue = new SQSQueue(
                new SQSClient.Simple(
                    this.options.valueOf("sqs-key").toString(),
                    this.options.valueOf("sqs-secret").toString(),
                    sqs
                )
            );
        } else {
            queue = new SQSQueue(new SQSClient.Assumed(sqs));
        }
        final SQSClient receipts;
        if (this.options.has("sqs-key")) {
            receipts = new SQSClient.Simple(
                this.options.valueOf("sqs-key").toString(),
                this.options.valueOf("sqs-secret").toString(),
                this.options.valueOf("sqs-wallet-url").toString()
            );
        } else {
            receipts = new SQSClient.Assumed(
                this.options.valueOf("sqs-wallet-url").toString()
            );
        }
        final Region region;
        if (this.options.has("dynamo-key")) {
            region = new Region.Prefixed(
                new Region.Simple(
                    new Credentials.Simple(
                        this.options.valueOf("dynamo-key").toString(),
                        this.options.valueOf("dynamo-secret").toString()
                    )
                ),
                this.options.valueOf("dynamo-prefix").toString()
            );
        } else {
            region = new Region.Prefixed(
                new Region.Simple(new Credentials.Assumed()),
                this.options.valueOf("dynamo-prefix").toString()
            );
        }
        return new SimpleConveyer(
            queue,
            new ClasspathRepo(),
            new AuditUsers(
                new PgUsers(
                    new PgClient.Simple(
                        this.options.valueOf("pgsql-url").toString(),
                        this.options.valueOf("pgsql-password").toString()
                    ),
                    receipts,
                    new AwsUsers(region, receipts)
                )
            ),
            Integer.parseInt(this.options.valueOf("threads").toString())
        );
    }

    /**
     * Make a local conveyer, for one spec.
     * @return Conveyer
     * @throws Exception If fails
     */
    private SimpleConveyer local() throws Exception {
        final Coordinates work = new Coordinates.Simple(
            URN.create("urn:facebook:1"), "default"
        );
        return new SimpleConveyer(
            new Queue() {
                private final transient AtomicBoolean done =
                    new AtomicBoolean();
                @Override
                public void push(final Coordinates work) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Coordinates pull(final int limit, final TimeUnit unit) {
                    final Coordinates pulled;
                    if (done.compareAndSet(false, true)) {
                        pulled = work;
                        done.set(true);
                    } else {
                        pulled = new Coordinates.None();
                    }
                    return pulled;
                }
            },
            new ClasspathRepo(),
            new FakeUsers(
                work,
                new Spec.Simple(
                    FileUtils.readFileToString(
                        new File(this.options.valueOf("spec").toString()),
                        CharEncoding.UTF_8
                    )
                )
            ),
            1
        );
    }

}
