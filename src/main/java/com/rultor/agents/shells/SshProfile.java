/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Profile decorator that fills in missing SSH entries.
 *
 * <p>When the origin profile has no ssh entry, or misses some of its host,
 * port, login or key sub-entries, this decorator injects them from
 * {@link SshDefaults}, so consumers can resolve all SSH settings from the
 * profile alone, see {@link PfShell}.</p>
 *
 * @since 2.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "dfts" })
public final class SshProfile implements Profile {

    /**
     * Origin profile.
     */
    private final transient Profile origin;

    /**
     * SSH defaults.
     */
    private final transient SshDefaults dfts;

    /**
     * Ctor.
     *
     * @param orgn Origin profile
     * @param dfts SSH defaults, for entries missing in the profile
     */
    public SshProfile(final Profile orgn, final SshDefaults dfts) {
        this.origin = orgn;
        this.dfts = dfts;
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    public String defaultBranch() {
        return this.origin.defaultBranch();
    }

    @Override
    public Map<String, InputStream> assets() throws IOException {
        return this.origin.assets();
    }

    @Override
    public XML read() throws IOException {
        final XML xml = this.origin.read();
        try {
            return new XMLDocument(
                new Xembler(this.dfts.fill(xml))
                    .apply(new XMLDocument(xml.toString()).deepCopy())
            );
        } catch (final ImpossibleModificationException ex) {
            throw new Profile.ConfigException(ex);
        }
    }
}
