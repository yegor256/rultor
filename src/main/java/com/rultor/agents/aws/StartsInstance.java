/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Starts EC2 instance.
 *
 * @since 1.77
 */
@Immutable
@ToString
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
public final class StartsInstance extends AbstractAgent {

    /**
     * AWS Ec2 Instance.
     */
    private final AwsEc2Image image;

    /**
     * Name Tag value.
     */
    private final String tag;

    /**
     * Shell in profile.
     */
    private final transient PfShell shell;

    /**
     * Ctor.
     * @param image Instance image to run
     * @param profile Profile
     * @param port Default Port of server
     * @param user Default Login
     * @param key Default Private SSH key
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public StartsInstance(final AwsEc2Image image, final Profile profile,
        final int port, final String user, final String key) {
        this(image, "rultor", profile, port, user, key);
    }

    /**
     * Ctor.
     * @param image Instance image to run
     * @param tag Name tag value
     * @param profile Profile
     * @param port Default Port of server
     * @param user Default Login
     * @param key Default Private SSH key
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public StartsInstance(final AwsEc2Image image, final String tag,
        final Profile profile, final int port,
        final String user, final String key) {
        super("/talk[daemon and not(shell)]");
        if (user.isEmpty()) {
            throw new IllegalArgumentException(
                "User name is mandatory"
            );
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                "SSH key is mandatory"
            );
        }
        this.image = image;
        this.tag = tag;
        this.shell = new PfShell(profile, "", port, user, key);
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String hash = xml.xpath("/talk/daemon/@id").get(0);
        final Directives dirs = new Directives();
        try {
            final String login = this.shell.login();
            if (login.isEmpty()) {
                throw new Profile.ConfigException(
                    "SSH login is empty, it's a mistake"
                );
            }
            final String key = this.shell.key();
            if (key.isEmpty()) {
                throw new Profile.ConfigException(
                    "SSH key is empty, it's a mistake"
                );
            }
            final AwsEc2Instance inst = this.image.run();
            if (!this.tag.isEmpty()) {
                inst.tag("Name", this.tag);
            }
            Logger.info(
                this, "EC2 instance %s on %s started in %s",
                inst.id(), inst.address(),
                xml.xpath("/talk/@name").get(0)
            );
            dirs.xpath("/talk").add("ec2")
                .attr("id", inst.id());
            dirs.xpath("/talk").add("shell")
                .attr("id", hash)
                .add("host").set(inst.address()).up()
                .add("port").set(Integer.toString(this.shell.port())).up()
                .add("login").set(login).up()
                .add("key").set(key);
        } catch (final Profile.ConfigException ex) {
            dirs.xpath("/talk/daemon/script").set(
                String.format(
                    "Failed to read profile: %s", ex.getLocalizedMessage()
                )
            );
        }
        return dirs;
    }
}
