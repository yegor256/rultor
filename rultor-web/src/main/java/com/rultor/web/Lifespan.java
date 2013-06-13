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
package com.rultor.web;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.manifests.Manifests;
import com.rultor.aws.DynamoUsers;
import com.rultor.aws.S3Log;
import com.rultor.conveyer.SimpleConveyer;
import com.rultor.repo.ClasspathRepo;
import com.rultor.spi.Queue;
import com.rultor.spi.Repo;
import com.rultor.spi.Unit;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;

/**
 * Lifespan.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Loggable(Loggable.INFO)
public final class Lifespan implements ServletContextListener {

    /**
     * SimpleConveyer.
     */
    private transient SimpleConveyer conveyer;

    /**
     * Quartz the works.
     */
    private transient Quartz quartz;

    /**
     * Log.
     */
    private transient S3Log log;

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        try {
            Manifests.append(event.getServletContext());
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
        final String key = Manifests.read("Rultor-S3Key");
        final String secret = Manifests.read("Rultor-S3Secret");
        final Region region = new Region.Prefixed(
            new Region.Simple(new Credentials.Simple(key, secret)),
            Manifests.read("Rultor-DynamoPrefix")
        );
        this.log = new S3Log(
            key, secret,
            Manifests.read("Rultor-S3Bucket")
        );
        final Users users = new DynamoUsers(region, this.log);
        final Repo repo = new ClasspathRepo();
        event.getServletContext().setAttribute(Users.class.getName(), users);
        event.getServletContext().setAttribute(Repo.class.getName(), repo);
        final Queue queue = new Queue.Memory();
        this.quartz = new Lifespan.Quartz(users, queue);
        this.conveyer = new SimpleConveyer(queue, repo, this.log);
        this.conveyer.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        IOUtils.closeQuietly(this.quartz);
        IOUtils.closeQuietly(this.conveyer);
        IOUtils.closeQuietly(this.log);
    }

    /**
     * Every minute feeds all specs to queue.
     */
    @Loggable(Loggable.INFO)
    @ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.MINUTES)
    @EqualsAndHashCode(of = { "users", "queue" })
    @SuppressWarnings("PMD.DoNotUseThreads")
    private static final class Quartz implements Runnable, Closeable {
        /**
         * Users.
         */
        private final transient Users users;
        /**
         * Queue.
         */
        private final transient Queue queue;
        /**
         * Public ctor.
         * @param usr Users
         * @param que Queue
         */
        protected Quartz(final Users usr, final Queue que) {
            this.users = usr;
            this.queue = que;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public void run() {
            for (User user : this.users.everybody()) {
                for (Map.Entry<String, Unit> entry : user.units().entrySet()) {
                    this.queue.push(
                        new Work.Simple(
                            user.urn(),
                            entry.getKey(),
                            entry.getValue().spec()
                        )
                    );
                }
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            // nothing to do
        }
    }

}
