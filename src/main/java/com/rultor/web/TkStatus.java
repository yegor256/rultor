/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

import com.jcabi.log.Logger;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tick;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.cactoos.list.ListOf;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithStatus;

/**
 * Status (OK or not OK).
 *
 * @since 1.52
 */
final class TkStatus implements Take {

    /**
     * When we started.
     */
    private final transient long start;

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
        this.start = System.currentTimeMillis();
    }

    @Override
    public Response act(final Request req) {
        final List<Tick> ticks = new ListOf<>(this.pulse.ticks());
        final StringBuilder msg = new StringBuilder(1_000);
        msg.append(
            Logger.format(
                "Up for %[ms]s already\n",
                System.currentTimeMillis() - this.start
            )
        );
        final Response response;
        if (ticks.isEmpty()) {
            response = new RsWithStatus(HttpURLConnection.HTTP_OK);
            msg.append("There is no activity yet, refresh in a few seconds");
        } else {
            final long age = System.currentTimeMillis()
                - ticks.get(ticks.size() - 1).start();
            if (age > TimeUnit.MINUTES.toMillis(5L)) {
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
                response = new RsWithStatus(HttpURLConnection.HTTP_OK);
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
