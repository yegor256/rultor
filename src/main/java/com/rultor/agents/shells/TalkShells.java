/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.net.UnknownHostException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Shells referenced from Talks.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "xml")
public final class TalkShells {

    /**
     * Encapsulated XML.
     */
    private final transient XML xml;

    /**
     * Ctor.
     * @param talk XML in talk
     */
    public TalkShells(final XML talk) {
        this.xml = talk;
    }

    /**
     * Find and get shell.
     * @return Issue
     * @throws UnknownHostException If fails
     */
    public Shell get() throws UnknownHostException {
        final XML shell = this.xml.nodes("/talk/shell").get(0);
        return new PfShell(
            Profile.EMPTY,
            shell.xpath("host/text()").get(0),
            Integer.parseInt(shell.xpath("port/text()").get(0)),
            shell.xpath("login/text()").get(0),
            shell.xpath("key/text()").get(0)
        ).toSsh();
    }
}
