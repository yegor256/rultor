/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.web;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Timeable;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rexsl.page.BasePage;
import com.rexsl.page.BaseResource;
import com.rexsl.page.Inset;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.Resource;
import com.rexsl.page.auth.AuthInset;
import com.rexsl.page.auth.Facebook;
import com.rexsl.page.auth.Github;
import com.rexsl.page.auth.Google;
import com.rexsl.page.auth.HttpBasic;
import com.rexsl.page.auth.Identity;
import com.rexsl.page.auth.Provider;
import com.rexsl.page.inset.FlashInset;
import com.rexsl.page.inset.LinksInset;
import com.rexsl.page.inset.VersionInset;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Resource.Forwarded
@Loggable(Loggable.DEBUG)
@Inset.Default(LinksInset.class)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
public class BaseRs extends BaseResource {

    /**
     * Authentication keys.
     */
    private static final AuthKeys KEYS = new AuthKeys();

    /**
     * Test authentication provider.
     */
    private static final Provider TEST_PROVIDER = new Provider() {
        @Override
        public Identity identity() throws IOException {
            final Identity identity;
            if ("12345".equals(Manifests.read("Rultor-Revision"))) {
                identity = new Identity.Simple(
                    URN.create("urn:facebook:1"),
                    "Local Host",
                    URI.create("http://img.rultor.com/none.png")
                );
            } else {
                identity = Identity.ANONYMOUS;
            }
            return identity;
        }
    };

    /**
     * Flash.
     * @return The inset with flash
     */
    @Inset.Runtime
    @NotNull(message = "flash can never be NULL")
    public final FlashInset flash() {
        return new FlashInset(this);
    }

    /**
     * Inset with a version of the product.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "version can never be NULL")
    public final Inset insetVersion() {
        return new VersionInset(
            Manifests.read("Rultor-Version"),
            // @checkstyle MultipleStringLiterals (1 line)
            Manifests.read("Rultor-Revision"),
            Manifests.read("Rultor-Date")
        );
    }

    /**
     * Supplementary inset.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "supplementary inset can never be NULL")
    public final Inset insetSupplementary() {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                builder.type(MediaType.TEXT_XML);
                builder.header(HttpHeaders.VARY, "Cookie");
                builder.header(
                    "X-Rultor-Revision",
                    Manifests.read("Rultor-Revision")
                );
            }
        };
    }

    /**
     * User financial stats.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "finance inset can never be NULL")
    public final Inset insetFinances() {
        // @checkstyle AnonInnerLength (50 lines)
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                if (!BaseRs.this.auth().identity().equals(Identity.ANONYMOUS)) {
                    page.link(
                        new Link(
                            "account",
                            BaseRs.this.uriInfo().getBaseUriBuilder()
                                .clone()
                                .path(AccountRs.class)
                                .build()
                        )
                    );
                    page.append(
                        new JaxbBundle(
                            "balance",
                            BaseRs.this.balance().toString()
                        )
                    );
                }
            }
        };
    }

    /**
     * Nagivation links.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "navigation inset can never be NULL")
    public final Inset insetNavigation() {
        // @checkstyle AnonInnerLength (50 lines)
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                if (!BaseRs.this.auth().identity().equals(Identity.ANONYMOUS)) {
                    page.link(
                        new Link(
                            "stands",
                            BaseRs.this.uriInfo().getBaseUriBuilder()
                                .clone()
                                .path(StandsRs.class)
                                .build()
                        )
                    );
                    page.link(
                        new Link(
                            "rules",
                            BaseRs.this.uriInfo().getBaseUriBuilder()
                                .clone()
                                .path(RulesRs.class)
                                .build()
                        )
                    );
                    page.append(
                        new Menu()
                            .with("home", "Home")
                            .with("rules", "Rules")
                            .with("stands", "Stands")
                            .with("account", "Account Statistics")
                            .with("auth-logout", "Log out")
                            .bundle()
                    );
                }
            }
        };
    }

    /**
     * Authentication key inset.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "auth key inset can never be NULL")
    public final Inset insetAuthKey() {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                final Identity identity = BaseRs.this.auth().identity();
                if (!identity.equals(Identity.ANONYMOUS)) {
                    page.append(
                        new JaxbBundle("api-key", BaseRs.KEYS.make(identity))
                    );
                }
            }
        };
    }

    /**
     * Authentication inset.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "auth inset can never be NULL")
    public final AuthInset auth() {
        // @checkstyle LineLength (4 lines)
        return new AuthInset(this, Manifests.read("Rultor-SecurityKey"))
            .with(new Facebook(this, Manifests.read("Rultor-FbId"), Manifests.read("Rultor-FbSecret")))
            .with(new Github(this, Manifests.read("Rultor-GithubId"), Manifests.read("Rultor-GithubSecret")))
            .with(new Google(this, Manifests.read("Rultor-GoogleId"), Manifests.read("Rultor-GoogleSecret")))
            .with(new HttpBasic(this, BaseRs.KEYS))
            .with(BaseRs.TEST_PROVIDER);
    }

    /**
     * Get currently logged in user.
     * @return The user
     */
    @NotNull(message = "User can't be NULL")
    protected final User user() {
        final Identity self = this.auth().identity();
        if (self.equals(Identity.ANONYMOUS)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "please login first",
                Level.WARNING
            );
        }
        return this.users().get(self.urn());
    }

    /**
     * Get all users.
     * @return The users
     */
    @NotNull(message = "USERS is not injected into servlet context")
    protected final Users users() {
        return Users.class.cast(
            this.servletContext().getAttribute(Users.class.getName())
        );
    }

    /**
     * Get repo.
     * @return Repo
     */
    @NotNull(message = "REPO is not injected into servlet context")
    protected final Repo repo() {
        return Repo.class.cast(
            this.servletContext().getAttribute(Repo.class.getName())
        );
    }

    /**
     * The work we're in (while rendering).
     * @param rule Unit being rendered
     * @param spec Its spec
     * @return The work
     */
    protected final Work work(final String rule, final Spec spec) {
        // @checkstyle AnonInnerLength (50 lines)
        return new Work() {
            @Override
            public Time scheduled() {
                return new Time();
            }
            @Override
            public URN owner() {
                return BaseRs.this.user().urn();
            }
            @Override
            public String rule() {
                return rule;
            }
            @Override
            public URI stdout() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Get balance of the current user.
     * @return His balance
     */
    @Timeable
    private Dollars balance() {
        return this.user().account().balance();
    }

}
