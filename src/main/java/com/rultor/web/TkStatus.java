/**
 * Copyright (c) 2009-2015, rultor.com
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

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tick;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithStatus;

/**
 * Status (OK or not OK).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.52
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkStatus implements Take {

    /**
     * Pulse.
     */
    private final transient Pulse pulse;

    /**
     * Ctor.
     * @param pls Pulse
     */
    TkStatus(final Pulse pls) {
        this.pulse = pls;
    }

    @Override
    public Response act() {
        final Iterable<Tick> ticks = this.pulse.ticks();
        final Response response;
        if (Iterables.isEmpty(ticks)) {
            response = new RsWithBody(
                new RsWithStatus(HttpURLConnection.HTTP_INTERNAL_ERROR),
                "there is no activity yet"
            );
        } else {
            final long age = System.currentTimeMillis()
                - Iterables.getLast(ticks).start();
            if (age > TimeUnit.MINUTES.toMillis((long) Tv.FIVE)) {
                response = new RsWithBody(
                    new RsWithStatus(HttpURLConnection.HTTP_INTERNAL_ERROR),
                    Logger.format("the system is down, for %[ms]s", age)
                );
            } else {
                response = new RsWithBody(
                    Logger.format("it is up and running, %[ms]s", age)
                );
            }
        }
        return response;
    }

}
