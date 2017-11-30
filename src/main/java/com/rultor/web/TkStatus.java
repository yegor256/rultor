/**
 * Copyright (c) 2009-2018, rultor.com
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

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tick;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import org.cactoos.list.SolidList;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsEmpty;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithStatus;

/**
 * Status (OK or not OK).
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
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
    public Response act(final Request req) {
        final SolidList<Tick> ticks = new SolidList<>(this.pulse.ticks());
        final StringBuilder msg = new StringBuilder(Tv.THOUSAND);
        final Response response;
        if (ticks.isEmpty()) {
            response = new RsWithStatus(HttpURLConnection.HTTP_NO_CONTENT);
            msg.append("There is no activity yet, refresh in a few seconds");
        } else {
            final long age = System.currentTimeMillis()
                - ticks.get(ticks.size() - 1).start();
            if (age > TimeUnit.MINUTES.toMillis((long) Tv.FIVE)) {
                response = new RsWithStatus(
                    HttpURLConnection.HTTP_INTERNAL_ERROR
                );
                msg.append(
                    Logger.format(
                        "Unfortunately, the system is down, for %[ms]s already",
                        age
                    )
                );
                msg.append(
                    "\n\nPlease, email this page to bug@rultor.com"
                );
            } else {
                response = new RsEmpty();
                msg.append(
                    Logger.format(
                        "It is up and running, last check done %[ms]s ago",
                        age
                    )
                );
            }
        }
        for (final Throwable error : this.pulse.error()) {
            msg.append(Logger.format("\n\n%[exception]s", error));
        }
        return new RsWithBody(response, msg.toString());
    }

}
