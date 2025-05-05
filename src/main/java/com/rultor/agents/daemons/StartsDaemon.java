/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Timeable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.Env;
import com.rultor.Time;
import com.rultor.agents.Required;
import com.rultor.agents.shells.TalkShells;
import com.rultor.profiles.ProfileDeprecations;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Starts daemon.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("PMD.ExcessiveImports")
public final class StartsDaemon implements Agent {

    /**
     * Gpg home dir.
     */
    public static final String GPG_HOME = ".gnupg";

    /**
     * Paths to match.
     */
    private static final Array<String> PATHS = new Array<>(
        "/talk/shell[host and port and login and key]",
        "/talk/daemon[script and dir and not(started) and not(ended)]",
        "/talk/daemon[dir != '']"
    );

    /**
     * Profile to get assets from.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param prof Profile
     */
    public StartsDaemon(final Profile prof) {
        this.profile = prof;
    }

    @Override
    public void execute(final Talk talk) throws IOException {
        if (new Required(StartsDaemon.PATHS).isIt(talk)) {
            talk.modify(
                new Directives()
                    .xpath("/talk/daemon[not(started)]")
                    .strict(1)
                    .add("started").set(new Time().iso())
            );
            talk.modify(this.process(talk.read()));
        }
    }

    /**
     * Process talk.
     *
     * The annotation here is a TEMPORARY solution. It will be removed in the
     * future. We need it because the SSH shell is not dropping the connection
     * when the command is in the background.
     *
     * @param xml The XML to process.
     * @return List of directives
     */
    @Timeable(limit = 1, unit = TimeUnit.MINUTES)
    public Iterable<Directive> process(final XML xml) {
        final Directives dirs = new Directives()
            .xpath("/talk/daemon[not(ended)]")
            .strict(1);
        try {
            this.run(xml);
        } catch (final IOException ex) {
            dirs.add("ended").set(new Time().iso()).up()
                .add("code").set("128").up()
                .add("tail").set(ex.getLocalizedMessage());
            Logger.warn(this, "%[exception]s", ex);
        }
        return dirs;
    }

    /**
     * Run daemon.
     * @param xml XML with talk
     * @return Directory where it started
     * @throws IOException If fails
     */
    @RetryOnFailure
    public String run(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        new ProfileDeprecations(this.profile).print(shell);
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        final XML daemon = xml.nodes("/talk/daemon").get(0);
        final String script = String.join(
            "\n",
            "#!/bin/bash",
            "set -x",
            "set -e",
            "set -o pipefail",
            "cd $(dirname $0)",
            "echo $$ > pid",
            String.format(
                "echo %s",
                Ssh.escape(
                    String.format(
                        "%s %s",
                        Env.read("Rultor-Version"),
                        Env.read("Rultor-Revision")
                    )
                )
            ),
            "date",
            "uptime",
            this.upload(shell, dir),
            daemon.xpath("script/text()").get(0)
        );
        new Shell.Safe(shell).exec(
            String.format("cd %s; cat > run.sh", Ssh.escape(dir)),
            IOUtils.toInputStream(script, StandardCharsets.UTF_8),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
        shell.exec(
            String.join(
                " &&  ",
                "gpg --import",
                "gpg --version",
                "gpgconf --reload gpg-agent",
                "gpg --list-keys"
            ),
            this.getClass().getResourceAsStream(
                "/com/rultor/agents/daemons/secring.gpg.base64"
            ),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
        new Shell.Empty(new Shell.Safe(shell)).exec(
            String.join(
                " && ",
                String.format("cd %s", Ssh.escape(dir)),
                "chmod a+x run.sh",
                "echo 'run.sh failed to start' > stdout",
                // @checkstyle LineLength (1 line)
                "( ( nohup ./run.sh </dev/null >stdout 2>&1; echo $? >status ) </dev/null >/dev/null 2>&1 & )"
            )
        );
        Logger.info(this, "Daemon started at %s", dir);
        return dir;
    }

    /**
     * Upload assets.
     * @param shell Shell
     * @param dir Directory
     * @return Script to use
     * @throws IOException If fails
     */
    private String upload(final Shell shell, final String dir)
        throws IOException {
        final long start = System.currentTimeMillis();
        String script = "";
        try {
            for (final Map.Entry<String, InputStream> asset
                : this.profile.assets().entrySet()) {
                shell.exec(
                    String.format(
                        "cat > %s",
                        Ssh.escape(String.format("%s/%s", dir, asset.getKey()))
                    ),
                    asset.getValue(),
                    Logger.stream(Level.INFO, true),
                    Logger.stream(Level.WARNING, true)
                );
                if (Logger.isInfoEnabled(this)) {
                    Logger.info(
                        this, "\"%s\" uploaded into %s in %[ms]s",
                        asset.getKey(), dir,
                        System.currentTimeMillis() - start
                    );
                }
            }
        } catch (final Profile.ConfigException ex) {
            script = Logger.format(
                "cat << EOT\n%s\nEOT\nexit -1",
                ex.getLocalizedMessage()
            );
        }
        return script;
    }

}
