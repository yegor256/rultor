/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rexsl.page.BasePage;
import com.rexsl.page.BaseResource;
import com.rexsl.page.Inset;
import com.rexsl.page.Link;
import com.rexsl.page.Resource;
import com.rexsl.page.auth.AuthInset;
import com.rexsl.page.auth.Facebook;
import com.rexsl.page.auth.Github;
import com.rexsl.page.auth.Google;
import com.rexsl.page.auth.Identity;
import com.rexsl.page.auth.Provider;
import com.rexsl.page.inset.FlashInset;
import com.rexsl.page.inset.LinksInset;
import com.rexsl.page.inset.VersionInset;
import com.rultor.spi.Base;
import com.rultor.spi.User;
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
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@Resource.Forwarded
@Inset.Default(LinksInset.class)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
public class BaseRs extends BaseResource {

    /**
     * Test user.
     */
    public static final URN TEST_URN = URN.create("urn:facebook:1");

    /**
     * Test authentication provider.
     */
    private static final Provider TEST_PROVIDER = new Provider() {
        @Override
        public Identity identity() {
            final Identity identity;
            if (!Manifests.read("Rultor-DynamoKey").startsWith("AAAAA")) {
                identity = new Identity.Simple(
                    BaseRs.TEST_URN,
                    "localhost",
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
                page.link(new Link("root", "/"));
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
                            "repos",
                            BaseRs.this.uriInfo().getBaseUriBuilder()
                                .clone()
                                .path(ReposRs.class)
                                .build()
                        )
                    );
                    page.link(
                        new Link(
                            "assets",
                            BaseRs.this.uriInfo().getBaseUriBuilder()
                                .clone()
                                .path(ReposRs.class)
                                .build()
                        )
                    );
                    page.append(
                        new Menu()
                            .with("home", "Home")
                            .with("repos", "Repositories")
                            .with("assets", "Assets")
                            .with("auth-logout", "Log out")
                            .bundle()
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
        return this.base().user(self.urn());
    }

    /**
     * Get base.
     * @return The users
     */
    @NotNull(message = "BASE is not injected into servlet context")
    protected final Base base() {
        return Base.class.cast(
            this.servletContext().getAttribute(Base.class.getName())
        );
    }

}
