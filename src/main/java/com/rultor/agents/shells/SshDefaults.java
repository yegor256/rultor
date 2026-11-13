/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import org.xembly.Directives;

/**
 * Default SSH settings, to use when a profile doesn't specify them.
 *
 * @since 2.1
 */
@Immutable
public final class SshDefaults {

    /**
     * Default host name or IP address.
     */
    private final transient String host;

    /**
     * Default port of server.
     */
    private final transient int port;

    /**
     * Default login.
     */
    private final transient String login;

    /**
     * Default private SSH key.
     */
    private final transient String key;

    /**
     * Ctor.
     *
     * @param host Default host name or IP address
     * @param port Default port of server
     * @param login Default login
     * @param key Default private SSH key
     */
    public SshDefaults(final String host, final int port,
        final String login, final String key) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.key = key;
    }

    /**
     * Directives filling in the SSH entries missing in the profile XML.
     *
     * <p>Every entry injected by this method is marked with a
     * {@code default='true'} attribute, so consumers can distinguish an
     * injected default from a value that the profile configured itself,
     * e.g. an asset reference in the {@code key} entry.</p>
     *
     * @param xml Profile XML
     * @return Directives, possibly empty
     */
    public Directives fill(final XML xml) {
        final Directives dirs = new Directives();
        if (xml.nodes("/p/entry[@key='ssh']").isEmpty()) {
            dirs.xpath("/p").add("entry").attr("key", "ssh");
        }
        SshDefaults.entry(dirs, xml, "host", this.host);
        SshDefaults.entry(dirs, xml, "port", Integer.toString(this.port));
        SshDefaults.entry(dirs, xml, "login", this.login);
        SshDefaults.keyEntry(dirs, xml, this.key);
        return dirs;
    }

    private static void entry(final Directives dirs, final XML xml,
        final String name, final String value) {
        if (xml.nodes(
            String.format("/p/entry[@key='ssh']/entry[@key='%s']", name)
        ).isEmpty()) {
            dirs.xpath("/p/entry[@key='ssh']")
                .add("entry").attr("key", name)
                .attr("default", "true").set(value);
        }
    }

    private static void keyEntry(final Directives dirs, final XML xml,
        final String value) {
        final String xpath = "/p/entry[@key='ssh']/entry[@key='key']";
        if (xml.nodes(xpath).isEmpty()) {
            dirs.xpath("/p/entry[@key='ssh']")
                .add("entry").attr("key", "key")
                .attr("default", "true").set(value);
        } else if (xml.xpath(String.format("%s/text()", xpath)).isEmpty()) {
            dirs.xpath(xpath)
                .attr("default", "true").set(value);
        }
    }
}
