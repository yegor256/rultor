/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Release;
import com.jcabi.github.ReleaseAssets;
import com.jcabi.github.Releases;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Attach binaries to release.
 *
 * @since 1.3
 */
@Immutable
@ToString
public final class ReleaseBinaries extends AbstractAgent {

    /**
     * Default directory for build artifacts when unspecified.
     */
    private static final String ARTIFACTS_DIR = "target";

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Fallback comment template if resource bundle key is missing.
     */
    private static final String DEFAULT_MSG = "Attached %d binaries to the release.";

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param ghub Github client
     * @param prof Profile
     */
    public ReleaseBinaries(final Github ghub, final Profile prof) {
        super(
            "/talk/wire[github-repo and github-issue]",
            "/talk/request[@id and type='release' and success='true']"
        );
        this.github = ghub;
        this.profile = prof;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String tag = xml.xpath(
            "/talk/request/args/arg[@name='tag']/text()"
        ).get(0);
        final Issue.Smart issue = new TalkIssues(this.github, xml).get();
        final Releases.Smart rels =
            new Releases.Smart(issue.repo().releases());
        if (!rels.exists(tag)) {
            throw new IllegalStateException(
                String.format("Release '%s' not found", tag)
            );
        }
        final File ref = this.artifactsReference();
        final File[] files;
        if (ref.isFile()) {
            files = new File[]{ref};
        } else {
            files = ref.listFiles();
        }
        if (files == null || files.length == 0) {
            throw new IllegalStateException(
                String.format(
                    "No binaries found in: %s",
                    ref.getAbsolutePath()
                )
            );
        }
        final Release.Smart rel = new Release.Smart(rels.find(tag));
        final ReleaseAssets assets = rel.assets();
        int count = 0;
        for (final File file : files) {
            if (!file.isFile()) {
                continue;
            }
            final String name = file.getName();
            final String mime = mimeType(name);
            final byte[] content = Files.readAllBytes(file.toPath());
            assets.upload(content, mime, name);
            ++count;
        }
        String tpl;
        try {
            tpl = ReleaseBinaries.PHRASES.getString("ReleaseBinaries.attached");
        } catch (final MissingResourceException ex) {
            tpl = ReleaseBinaries.DEFAULT_MSG;
        }
        issue.comments().post(
            String.format(tpl, count)
        );
        return new Directives();
    }

    /**
     * Determine the artifact reference: either a specific file or a directory.
     * @return File reference to the artifact path
     * @throws IOException If reading the profile fails
     */
    private File artifactsReference() throws IOException {
        final List<String> list = this.profile.read()
            .xpath("/p/entry[@key='artifacts']/text()");
        final String path;
        if (list.isEmpty()) {
            path = ReleaseBinaries.ARTIFACTS_DIR;
        } else {
            path = list.get(0).trim();
        }
        final File ref = new File(path);
        if (!ref.exists()) {
            throw new IllegalStateException(
                String.format(
                    "Artifact path does not exist: %s",
                    path
                )
            );
        }
        return ref;
    }

    /**
     * Simple heuristic to determine the MIME type based on file extension.
     * @param name File name
     * @return Corresponding MIME type
     */
    private static String mimeType(final String name) {
        final String mime;
        if (name.endsWith(".jar")) {
            mime = "application/java-archive";
        } else if (name.endsWith(".zip")) {
            mime = "application/zip";
        } else if (name.endsWith(".tar.gz")) {
            mime = "application/gzip";
        } else {
            mime = "application/octet-stream";
        }
        return mime;
    }
}
