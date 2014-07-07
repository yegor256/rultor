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
package com.rultor.web;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.jcabi.manifests.Manifests;
import com.rultor.agents.Agents;
import com.rultor.dynamo.DyTalks;
import com.rultor.profiles.Profiles;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Lifespan.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode
@Loggable(Loggable.INFO)
public final class Lifespan implements ServletContextListener {

    /**
     * Routine worker.
     */
    private final transient ScheduledExecutorService service =
        Executors.newSingleThreadScheduledExecutor(new VerboseThreads());

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        try {
            Manifests.append(event.getServletContext());
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        final ServletContext context = event.getServletContext();
        final Talks talks = new DyTalks();
        context.setAttribute(Talks.class.getName(), talks);
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Lifespan.this.routine(talks);
                        return null;
                    }
                },
                true
            ),
            1L, 1L,
            TimeUnit.MINUTES
        );
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        this.service.shutdown();
    }

    /**
     * Routine every-minute proc.
     * @param talks Talks
     * @throws IOException If fails
     */
    private void routine(final Talks talks) throws IOException {
        final Agents agents = new Agents();
        for (final SuperAgent agent : agents.supers()) {
            agent.execute(talks);
        }
        final Profiles profiles = new Profiles();
        for (final Talk talk : talks.active()) {
            final Profile profile = profiles.fetch(talk);
            for (final Agent agent : agents.agents(profile)) {
                agent.execute(talk);
            }
        }
    }

}
