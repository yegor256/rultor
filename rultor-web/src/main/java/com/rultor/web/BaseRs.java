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
import com.rultor.client.RestUser;
import com.rultor.spi.Repo;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.Cookie;
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
 */
@Resource.Forwarded
@Loggable(Loggable.DEBUG)
@Inset.Default(LinksInset.class)
public class BaseRs extends BaseResource {

    /**
     * Flash.
     * @return The inset with flash
     */
    @Inset.Runtime
    public final FlashInset flash() {
        return new FlashInset(this);
    }

    /**
     * Inset with a version of the product.
     * @return The inset
     */
    @Inset.Runtime
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
    public final Inset insetSupplementary() {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                builder.type(MediaType.TEXT_XML);
                builder.header(HttpHeaders.VARY, "Cookie");
            }
        };
    }

    /**
     * Authentication inset.
     * @return The inset
     */
    @Inset.Runtime
    public final AuthInset auth() {
        // @checkstyle LineLength (4 lines)
        final AuthInset auth = new AuthInset(this, Manifests.read("Rultor-SecurityKey"), Manifests.read("Rultor-SecuritySalt"))
            .with(new Facebook(this, Manifests.read("Rultor-FbId"), Manifests.read("Rultor-FbSecret")))
            .with(new Github(this, Manifests.read("Rultor-GithubId"), Manifests.read("Rultor-GithubSecret")))
            .with(new Google(this, Manifests.read("Rultor-GoogleId"), Manifests.read("Rultor-GoogleSecret")));
        if (Manifests.read("Rultor-DynamoKey").matches("[A-Z0-9]{20}")
            && "12345".equals(Manifests.read("Rultor-Revision"))) {
            auth.with(
                new Provider.Always(
                    new Identity.Simple(
                        URN.create("urn:facebook:1"),
                        "localhost",
                        URI.create("http://img.rultor.com/localhost.png")
                    )
                )
            );
        }
        auth.with(
            new Provider() {
                @Override
                public Link link() {
                    return new Link("cookie-auth", "/");
                }
                @Override
                public Identity identity() throws IOException {
                    final Cookie cookie = BaseRs.this.httpHeaders()
                        .getCookies().get(RestUser.COOKIE);
                    Identity identity = Identity.ANONYMOUS;
                    if (cookie != null) {
                        identity = new Identity.Simple(
                            URN.create(cookie.getValue()), "", URI.create("#")
                        );
                    }
                    return identity;
                }
            }
        );
        return auth;
    }

    /**
     * Get all users.
     * @return Cycles
     */
    protected final User user() {
        final Users users = Users.class.cast(
            this.servletContext().getAttribute(Users.class.getName())
        );
        if (users == null) {
            throw new IllegalStateException("USERS is not initialized");
        }
        return users.fetch(this.auth().identity().urn());
    }

    /**
     * Get repo.
     * @return Repo
     */
    protected final Repo repo() {
        final Repo repo = Repo.class.cast(
            this.servletContext().getAttribute(Repo.class.getName())
        );
        if (repo == null) {
            throw new IllegalStateException("REPO is not initialized");
        }
        return repo;
    }

}
