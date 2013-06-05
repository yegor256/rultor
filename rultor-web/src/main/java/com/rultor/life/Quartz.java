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

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.rultor.om.Unit;
import com.rultor.om.User;
import com.rultor.om.Users;
import com.rultor.queue.Queue;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * Quartz.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.INFO)
@ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.MINUTES)
@EqualsAndHashCode(of = { "users", "queue" })
@SuppressWarnings("PMD.DoNotUseThreads")
final class Quartz implements Runnable, Closeable {

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
    public void close() {
        // nothing to do
    }

}
