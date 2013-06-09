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
import com.jcabi.manifests.Manifests;
import com.rultor.conveyer.Conveyer;
import com.rultor.queue.Queue;
import com.rultor.repo.ClasspathRepo;
import com.rultor.repo.Repo;
import com.rultor.users.DynamoUsers;
import com.rultor.users.Unit;
import com.rultor.users.User;
import com.rultor.users.Users;
import java.io.Closeable;
import java.io.IOException;
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
 */
@Loggable(Loggable.INFO)
public final class Lifespan implements ServletContextListener {

    /**
     * Conveyer.
     */
    private transient Conveyer conveyer;

    /**
     * Quartz the works.
     */
    private transient Quartz quartz;

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
        final Users users = new DynamoUsers(
            Manifests.read("Rultor-DynamoKey"),
            Manifests.read("Rultor-DynamoSecret"),
            Manifests.read("Rultor-DynamoPrefix")
        );
        final Repo repo = new ClasspathRepo();
        event.getServletContext().setAttribute(Users.class.getName(), users);
        event.getServletContext().setAttribute(Repo.class.getName(), repo);
        final Queue queue = new Queue.Memory();
        this.quartz = new Lifespan.Quartz(users, queue);
        this.conveyer = new Conveyer(queue, repo);
        this.conveyer.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        IOUtils.closeQuietly(this.quartz);
        IOUtils.closeQuietly(this.conveyer);
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
        public void run() {
            for (User user : this.users.everybody()) {
                for (Unit unit : user.units().values()) {
                    this.queue.push(unit.spec());
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
