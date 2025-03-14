/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import co.stateful.Locks;
import co.stateful.Sttc;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.immutable.Array;
import com.jcabi.s3.Region;
import com.jcabi.s3.retry.ReRegion;
import com.jcabi.ssh.Ssh;
import com.rultor.Env;
import com.rultor.agents.aws.AwsEc2;
import com.rultor.agents.aws.ConnectsInstance;
import com.rultor.agents.aws.DescribesInstance;
import com.rultor.agents.aws.DetachesInstance;
import com.rultor.agents.aws.DropsInstance;
import com.rultor.agents.aws.PingsInstance;
import com.rultor.agents.aws.PrunesInstances;
import com.rultor.agents.aws.ShootsInstance;
import com.rultor.agents.aws.StartsInstance;
import com.rultor.agents.aws.TerminatesInstance;
import com.rultor.agents.daemons.ArchivesDaemon;
import com.rultor.agents.daemons.DismountDaemon;
import com.rultor.agents.daemons.DropsDaemon;
import com.rultor.agents.daemons.EndsDaemon;
import com.rultor.agents.daemons.KillsDaemon;
import com.rultor.agents.daemons.MkdirDaemon;
import com.rultor.agents.daemons.SanitizesDaemon;
import com.rultor.agents.daemons.StartsDaemon;
import com.rultor.agents.daemons.StopsDaemon;
import com.rultor.agents.daemons.WipesDaemon;
import com.rultor.agents.docker.DockerExec;
import com.rultor.agents.github.CommentsTag;
import com.rultor.agents.github.Dephantomizes;
import com.rultor.agents.github.DropsTalk;
import com.rultor.agents.github.Invitations;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.ReleaseBinaries;
import com.rultor.agents.github.Reports;
import com.rultor.agents.github.Stars;
import com.rultor.agents.github.StartsTalks;
import com.rultor.agents.github.Understands;
import com.rultor.agents.github.UnlocksRepo;
import com.rultor.agents.github.qtn.QnAlone;
import com.rultor.agents.github.qtn.QnAskedBy;
import com.rultor.agents.github.qtn.QnByArchitect;
import com.rultor.agents.github.qtn.QnConfig;
import com.rultor.agents.github.qtn.QnDeploy;
import com.rultor.agents.github.qtn.QnFirstOf;
import com.rultor.agents.github.qtn.QnFollow;
import com.rultor.agents.github.qtn.QnHello;
import com.rultor.agents.github.qtn.QnIamLost;
import com.rultor.agents.github.qtn.QnIfCollaborator;
import com.rultor.agents.github.qtn.QnIfContains;
import com.rultor.agents.github.qtn.QnIfPull;
import com.rultor.agents.github.qtn.QnIfUnlocked;
import com.rultor.agents.github.qtn.QnLock;
import com.rultor.agents.github.qtn.QnMerge;
import com.rultor.agents.github.qtn.QnNotSelf;
import com.rultor.agents.github.qtn.QnParametrized;
import com.rultor.agents.github.qtn.QnReaction;
import com.rultor.agents.github.qtn.QnReferredTo;
import com.rultor.agents.github.qtn.QnRelease;
import com.rultor.agents.github.qtn.QnSafe;
import com.rultor.agents.github.qtn.QnSince;
import com.rultor.agents.github.qtn.QnStatus;
import com.rultor.agents.github.qtn.QnStop;
import com.rultor.agents.github.qtn.QnUnlock;
import com.rultor.agents.github.qtn.QnVersion;
import com.rultor.agents.github.qtn.QnWithAuthor;
import com.rultor.agents.req.EndsRequest;
import com.rultor.agents.req.StartsRequest;
import com.rultor.agents.shells.PfShell;
import com.rultor.agents.shells.RegistersShell;
import com.rultor.agents.shells.RemovesShell;
import com.rultor.agents.twitter.OAuthTwitter;
import com.rultor.agents.twitter.Tweets;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * Agents.
 *
 * @since 1.0
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = {"github", "sttc"})
@SuppressWarnings("PMD.ExcessiveImports")
public final class Agents {

    /**
     * Host to connect to.
     */
    private static final String HOST = "b4.rultor.com";

    /**
     * Port to connect to.
     */
    private static final int PORT = 22;

    /**
     * Login in SSH session.
     */
    private static final String LOGIN = "rultor";

    /**
     * Github client.
     */
    private final transient Github github;

    /**
     * Sttc client.
     */
    private final transient Sttc sttc;

    /**
     * Ctor.
     * @param ghub Github client
     * @param stc Sttc client
     */
    public Agents(final Github ghub, final Sttc stc) {
        this.github = ghub;
        this.sttc = stc;
    }

    /**
     * Create super agent, starter.
     * @return The starter
     * @throws IOException If fails
     */
    public SuperAgent starter() throws IOException {
        final AwsEc2 aws = new AwsEc2(
            Env.read("Rultor-EC2Key"),
            Env.read("Rultor-EC2Secret")
        );
        return new SuperAgent.Iterative(
            new Array<>(
                new StartsTalks(this.github),
                new Invitations(this.github),
                new IndexesRequests(),
                new SuperAgent.Quiet(
                    new PrunesInstances(
                        aws, TimeUnit.HOURS.toMillis(3L)
                    )
                ),
                new SuperAgent.Disabled(
                    new DockerExec(
                        new Ssh(
                            Agents.HOST, Agents.PORT, Agents.LOGIN,
                            Agents.priv()
                        ),
                        "prune.sh"
                    )
                )
            )
        );
    }

    /**
     * Create super agent, closer.
     * @return The closer
     * @throws IOException If fails
     */
    public SuperAgent closer() throws IOException {
        return new SuperAgent.Iterative(
            new Array<>(
                new UnlocksRepo(this.sttc.locks(), this.github),
                new DeactivatesTalks()
            )
        );
    }

    /**
     * Create it for a talk.
     * @param talk Talk itself
     * @param profile Profile
     * @return The agent
     * @throws IOException If fails
     * @checkstyle MethodLengthCheck (500 lines)
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public Agent agent(final Talk talk, final Profile profile)
        throws IOException {
        final Locks locks = this.sttc.locks();
        final Question question = new QnSince(
            // @checkstyle MagicNumber (1 line)
            49_092_213,
            new QnNotSelf(
                new QnReferredTo(
                    this.github.users().self().login(),
                    new QnReaction(
                        new QnParametrized(
                            new QnWithAuthor(
                                new QnFollow(
                                    new QnFirstOf(
                                        new QnIfContains(
                                            "config", new QnConfig(profile)
                                        ),
                                        new QnIfContains(
                                            "status", new QnStatus(talk)
                                        ),
                                        new QnIfContains(
                                            "version", new QnVersion()
                                        ),
                                        new QnIfContains(
                                            "hello", new QnHello()
                                        ),
                                        new QnIfContains(
                                            "ping", new QnHello()
                                        ),
                                        new QnIfContains(
                                            "stop",
                                            new QnAskedBy(
                                                profile,
                                                Agents.commanders("stop"),
                                                new QnStop()
                                            )
                                        ),
                                        new QnIfCollaborator(
                                            new QnAlone(
                                                talk, locks,
                                                Agents.commands(profile)
                                            )
                                        ),
                                        new QnIamLost()
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
        final AwsEc2 aws = new AwsEc2(
            Env.read("Rultor-EC2Key"),
            Env.read("Rultor-EC2Secret")
        );
        return new VerboseAgent(
            new Agent.Iterative(
                new Agent.Quiet(new SanitizesDaemon()),
                new WipesDaemon(),
                new DropsTalk(),
                new Understands(
                    this.github,
                    new QnSafe(question)
                ),
                new StartsRequest(profile),
                new Agent.Quiet(
                    new Agent.Disabled(
                        new StartsInstance(
                            profile,
                            aws,
                            Env.read("Rultor-EC2Image"),
                            Env.read("Rultor-EC2Type"),
                            Env.read("Rultor-EC2Group"),
                            Env.read("Rultor-EC2Subnet")
                        ),
                        false
                    )
                ),
                new Agent.Quiet(new DescribesInstance(aws)),
                new Agent.Quiet(
                    new ConnectsInstance(
                        aws,
                        new PfShell(
                            profile,
                            "none",
                            Agents.PORT,
                            "ubuntu",
                            Agents.priv()
                        )
                    )
                ),
                new Agent.Disabled(
                    new RegistersShell(
                        profile,
                        Agents.HOST, Agents.PORT, Agents.LOGIN,
                        Agents.priv()
                    ),
                    true
                ),
                // @checkstyle MagicNumber (1 line)
                new DismountDaemon(TimeUnit.HOURS.toMinutes(5L)),
                new Agent.Quiet(
                    new DropsDaemon(TimeUnit.HOURS.toMinutes(1L))
                ),
                new Agent.Quiet(new MkdirDaemon()),
                new TimedAgent(new StartsDaemon(profile)),
                // @checkstyle MagicNumber (1 line)
                new Agent.SkipIfName(
                    new Agent.Quiet(new KillsDaemon(TimeUnit.HOURS.toMinutes(1L))),
                    "^(objectionary|yegor256|zerocracy)/.*$"
                ),
                new Agent.Quiet(new KillsDaemon(TimeUnit.HOURS.toMinutes(5L))),
                new TimedAgent(new StopsDaemon()),
                new TimedAgent(new Agent.Quiet(new EndsDaemon())),
                new EndsRequest(),
                new SafeAgent(
                    new Tweets(
                        this.github,
                        new OAuthTwitter(
                            Env.read("Rultor-TwitterKey"),
                            Env.read("Rultor-TwitterSecret"),
                            Env.read("Rultor-TwitterToken"),
                            Env.read("Rultor-TwitterTokenSecret")
                        )
                    )
                ),
                new CommentsTag(this.github, profile),
                new ReleaseBinaries(this.github, profile),
                new Dephantomizes(this.github),
                new Reports(this.github),
                new Agent.Quiet(new TerminatesInstance(aws)),
                new Agent.Quiet(new PingsInstance()),
                new Agent.Quiet(new DropsInstance(aws)),
                new Agent.Quiet(new DetachesInstance(aws)),
                new Agent.Quiet(new ShootsInstance(aws, TimeUnit.MINUTES.toMillis(15L))),
                new RemovesShell(),
                new Agent.Quiet(
                    new ArchivesDaemon(
                        new ReRegion(
                            new Region.Simple(
                                Env.read("Rultor-S3Key"),
                                Env.read("Rultor-S3Secret")
                            )
                        ).bucket(Env.read("Rultor-S3Bucket"))
                    )
                ),
                new Publishes(profile, this.github),
                new SafeAgent(new Stars(this.github))
            )
        );
    }

    /**
     * Handle main commands.
     * @param profile Profile to uuse
     * @return Array of questions.
     */
    private static Question commands(final Profile profile) {
        return new QnByArchitect(
            profile,
            "/p/entry[@key='architect']/item/text()",
            new QnFirstOf(
                new QnIfContains(
                    "unlock",
                    new QnUnlock()
                ),
                new QnIfContains(
                    "lock",
                    new QnLock()
                ),
                new QnIfContains(
                    "merge",
                    new QnAskedBy(
                        profile,
                        Agents.commanders("merge"),
                        new QnIfPull(new QnIfUnlocked(new QnMerge()))
                    )
                ),
                new QnIfContains(
                    "deploy",
                    new QnAskedBy(
                        profile,
                        Agents.commanders("deploy"),
                        new QnDeploy()
                    )
                ),
                new QnIfContains(
                    "release",
                    new QnAskedBy(
                        profile,
                        Agents.commanders("release"),
                        new QnRelease()
                    )
                )
            )
        );
    }

    /**
     * XPath for commanders.
     * @param entry Entry
     * @return XPath
     */
    private static String commanders(final String entry) {
        return String.format(
            "/p/entry[@key='%s']/entry[@key='commanders']/item/text()",
            entry
        );
    }

    /**
     * Make private ssh key.
     * @return The key
     */
    private static String priv() {
        String priv = System.getenv("PRIVATE_SSH_KEY");
        if (priv == null) {
            priv = new UncheckedText(
                new TextOf(
                    new ResourceOf("com/rultor/agents/rultor.key")
                )
            ).asString();
        }
        return priv;
    }

}
