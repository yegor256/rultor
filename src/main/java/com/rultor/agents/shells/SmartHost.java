/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Host.
 * @since 1.77.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "host")
final class SmartHost {

    /**
     * Server address.
     */
    private final transient InetAddress host;

    /**
     * Ctor.
     * @param address Host name or IP address
     * @throws UnknownHostException in case of address is not resolved
     */
    SmartHost(final String address) throws UnknownHostException {
        this(SmartHost.resolved(address));
    }

    /**
     * Ctor.
     * @param address Resolved host address
     */
    private SmartHost(final InetAddress address) {
        this.host = address;
    }

    /**
     * Host's IP.
     * @return Ip address
     * @checkstyle MethodNameCheck (3 lines)
     */
    String ip() {
        return this.host.getHostAddress();
    }

    /**
     * Resolve host address.
     * @param address Host name or IP address
     * @return Resolved address
     * @throws UnknownHostException in case of address is not resolved
     */
    private static InetAddress resolved(final String address)
        throws UnknownHostException {
        if (address.isEmpty()) {
            throw new IllegalArgumentException(
                "Host is mandatory"
            );
        }
        return InetAddress.getByName(address);
    }
}
