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
package com.rultor.conveyer;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rexsl.test.RestTester;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;

/**
 * Work with STDOUT specified.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "port", "key", "origin" })
final class StdoutWork implements Work {

    /**
     * Where to get public IP from EC2.
     */
    private static final URI META_IP = URI.create(
        "http://169.254.169.254/latest/meta-data/public-ipv4"
    );

    /**
     * Port we're listening to.
     */
    private final transient int port;

    /**
     * Streaming key.
     */
    private final transient String key;

    /**
     * Original work.
     */
    private final transient Work origin;

    /**
     * Public ctor.
     * @param prt Port we're at
     * @param auth Stream authentication key
     * @param wrk Original work
     */
    protected StdoutWork(final int prt, final String auth, final Work wrk) {
        this.port = prt;
        this.key = auth;
        this.origin = wrk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.origin.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Time scheduled() {
        return this.origin.scheduled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN owner() {
        return this.origin.owner();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String rule() {
        return this.origin.rule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI stdout() {
        return UriBuilder.fromUri("http://localhost/")
            .path("{key}")
            .host(StdoutWork.address())
            .port(this.port)
            .build(this.key);
    }

    /**
     * Fetch my public IP.
     * @return IP
     * @see http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-instance-addressing.html#using-instance-addressing-common
     */
    @Cacheable(forever = true)
    private static String address() {
        String address;
        try {
            address = RestTester.start(StdoutWork.META_IP)
                .get("fetch EC2 public IP").getBody();
        } catch (AssertionError ex) {
            address = "localhost";
        }
        return address;
    }

}
