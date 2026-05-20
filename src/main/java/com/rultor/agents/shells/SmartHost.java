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
     * @param address Already resolved internet address
     */
    SmartHost(final InetAddress address) {
        this.host = address;
    }

    /**
     * Create from host name or IP address string.
     * @param address Host name or IP address
     * @return SmartHost instance
     * @throws UnknownHostException in case of address is not resolved
     */
    static SmartHost create(final String address) throws UnknownHostException {
        if (address.isEmpty()) {
            throw new IllegalArgumentException(
                "Host is mandatory"
            );
        }
        return new SmartHost(InetAddress.getByName(address));
    }

    /**
     * Host's IP.
     * @return Ip address
     * @checkstyle MethodNameCheck (3 lines)
     */
    String ip() {
        return this.host.getHostAddress();
    }
}
