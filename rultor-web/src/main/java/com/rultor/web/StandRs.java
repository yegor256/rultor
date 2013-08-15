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
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.PageBuilder;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XSLT;
import com.rultor.spi.ACL;
import com.rultor.spi.Arguments;
import com.rultor.spi.Pulse;
import com.rultor.spi.Repo;
import com.rultor.spi.SpecException;
import com.rultor.spi.Stand;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.XemblySyntaxException;

/**
 * Stand front page.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/s/{stand:[\\w\\-]+}")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.ExcessiveImports")
public final class StandRs extends BaseRs {

    /**
     * Stand name.
     */
    private transient String name;

    /**
     * Inject it from query.
     * @param stand Stand name
     */
    @PathParam("stand")
    public void setName(@NotNull(message = "stand name can't be NULL")
        final String stand) {
        this.name = stand;
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/stand.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("stand", this.name))
            .append(this.pulses(this.stand().pulses().iterator(), Tv.TWENTY))
            .render()
            .build();
    }

    /**
     * Get stand.
     * @return The stand
     */
    private Stand stand() {
        final Stand stand;
        try {
            stand = this.users().stand(this.name);
        } catch (NoSuchElementException ex) {
            throw this.flash().redirect(this.uriInfo().getBaseUri(), ex);
        }
        if (!stand.owner().equals(this.user().urn())
            && !this.acl(stand).canView(this.user().urn())) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                String.format("access denied to stand `%s`", this.name),
                Level.SEVERE
            );
        }
        return stand;
    }

    /**
     * All pulses of the stand.
     * @param pulses All pulses to show
     * @param maximum Maximum to show
     * @return Collection of JAXB stands
     */
    private JaxbBundle pulses(final Iterator<Pulse> pulses, final int maximum) {
        JaxbBundle bundle = new JaxbBundle("pulses");
        int pos;
        for (pos = 0; pos < maximum; ++pos) {
            if (!pulses.hasNext()) {
                break;
            }
            bundle = bundle.add(this.pulse(pulses.next()));
        }
        return bundle;
    }

    /**
     * Convert pulse to JaxbBundle.
     * @param pulse The pulse
     * @return Bundle
     */
    private JaxbBundle pulse(final Pulse pulse) {
        JaxbBundle bundle = new JaxbBundle("pulse");
        try {
            final Snapshot snapshot = new Snapshot(
                new Directives(pulse.xembly())
                    .xpath("/snapshot/spec")
                    .remove()
                    .toString()
            );
            try {
                bundle = bundle.add(
                    new XSLT(
                        snapshot,
                        this.getClass().getResourceAsStream("post.xsl")
                    ).dom().getDocumentElement()
                );
            } catch (ImpossibleModificationException ex) {
                assert ex != null;
            } catch (TransformerException ex) {
                assert ex != null;
            }
        } catch (IOException ex) {
            assert ex != null;
        } catch (XemblySyntaxException ex) {
            assert ex != null;
        }
        return bundle;
    }

    /**
     * Get ACL of the stand.
     * @param stand The stand
     * @return ACL
     */
    private ACL acl(final Stand stand) {
        ACL acl;
        try {
            acl = ACL.class.cast(
                new Repo.Cached(this.repo(), this.user(), stand.acl())
                    .get()
                    .instantiate(
                        this.users(),
                        new Arguments(new Work.None(), new Wallet.Empty())
                    )
            );
        } catch (SpecException ex) {
            Logger.warn(this, ExceptionUtils.getRootCauseMessage(ex));
            acl = new ACL() {
                @Override
                public boolean canView(final URN urn) {
                    return false;
                }
                @Override
                public boolean canPost(final String key) {
                    return false;
                }
            };
        }
        return acl;
    }

}
